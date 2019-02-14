package com.fastlib.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fastlib.R;
import com.fastlib.annotation.ContentView;
import com.fastlib.net.Request;
import com.fastlib.utils.ImageUtil;
import com.fastlib.utils.LocalDataInject;
import com.fastlib.utils.N;
import com.fastlib.utils.ViewInject;
import com.fastlib.utils.permission.PermissionCallback;
import com.fastlib.utils.permission.RunTimePermissionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 17/1/31.
 * Fragment基本封装
 * 1.全局事件注册和解注册（EventObserver）
 * 2.视图属性和事件注解（Bind）
 * 3.线程池(mThreadPool)
 * 4.本地数据辅助（LocalData）
 * 5.相机相册调取（openAlbum(PhotoResultListener)和openCamera(PhotoResultListener))
 * 6.6.0权限获取辅助(RunTimePermissionUtil)
 * 7.进度提示框(LoadingDialog)
 * 8.快捷启动新页面（turnActivity、turnActivityForResult）
 */
public abstract class FastFragment extends Fragment{
    protected ThreadPoolExecutor mThreadPool;
    private volatile int mPreparedTaskRemain=3; //剩余初始化异步任务，当初始化异步任务全部结束时调用alreadyPrepared

    private boolean isGatingPhoto; //是否正在获取图像
    private PhotoResultListener mPhotoResultListener;
    private LocalDataInject mLocalDataInject;
    private LoadingDialog mLoading;
    private List<Request> mRequests=new ArrayList<>();

    protected abstract void alreadyPrepared(); //所有初始化任务结束

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalDataInject=new LocalDataInject(this);
        mThreadPool=(ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        mThreadPool.execute(new Runnable(){
            @Override
            public void run(){
                EventObserver.getInstance().subscribe(getContext(),FastFragment.this);
                prepareTask();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        final ContentView cv=getClass().getAnnotation(ContentView.class);
        if(cv!=null){
            final View root=inflater.inflate(cv.value(),null);
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ViewInject.inject(FastFragment.this,root,mThreadPool);
                    prepareTask();
                }
            });
            return root;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mThreadPool.execute(new Runnable(){
            @Override
            public void run() {
                mLocalDataInject.localDataInject();
                prepareTask();
            }
        });
    }

    /**
     * 启动网络请求
     * @param request
     */
    protected void net(Request request){
        if(!mRequests.contains(request))
            mRequests.add(request);
        request.setHost(this).setExecutor(mThreadPool).start(false);
    }

    public void addRequest(Request request){
        if(!mRequests.contains(request))
            mRequests.add(request);
    }

    /**
     * 开启获取相册照片
     * @param photoResultListener
     */
    protected void openAlbum(final PhotoResultListener photoResultListener) {
        RunTimePermissionUtil.onStorage(getActivity(), new PermissionCallback() {
            @Override
            public void onPermissionSuccess() {
                isGatingPhoto = true;
                mPhotoResultListener = photoResultListener;
                ImageUtil.openAlbum(FastFragment.this);
            }

            @Override
            public void onPermissionFailure(String hint) {
                N.showShort(getContext(),hint);
            }

        });
    }

    /**
     * 开启相机获取照片
     * @param photoResultListener
     */
    protected void openCamera(PhotoResultListener photoResultListener) {
        openCamera(photoResultListener, null);
    }

    /**
     * 开启相机获取照片并且指定存储位置
     * @param photoResultListener
     * @param file
     */
    public void openCamera(final PhotoResultListener photoResultListener, final File file) {
        RunTimePermissionUtil.onCamera(getActivity(), new PermissionCallback() {
            @Override
            public void onPermissionSuccess() {
                isGatingPhoto = true;
                mPhotoResultListener = photoResultListener;
                if (file == null)
                    ImageUtil.openCamera(FastFragment.this);
                else
                    ImageUtil.openCamera(FastFragment.this,file);
            }

            @Override
            public void onPermissionFailure(String hint) {
                N.showShort(getContext(),hint);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        mLocalDataInject.injectChildBack(data); //尝试获取有注解的子Activity返回数据
        if (isGatingPhoto) {
            isGatingPhoto = false;
            Uri photoUri = ImageUtil.getImageFromActive(requestCode, resultCode, data);
            if(photoUri != null){
                String photoPath = ImageUtil.getImageAbsolutePath(getContext(),photoUri);
                if (mPhotoResultListener != null)
                    mPhotoResultListener.onPhotoResult(photoPath);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPreparedTaskRemain=2;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventObserver.getInstance().unsubscribe(getContext(),this);
        mThreadPool.shutdownNow();
        mThreadPool.purge();
        for(Request request:mRequests)
            request.clear();
    }

    /**
     * 快捷启动一个Activity
     * @param cls 将启动的Activity类
     */
    public void turnActivity(Class<? extends Activity> cls){
        turnActivity(cls,null);
    }

    /**
     * 快捷启动一个Activity
     * @param cls 将启动的Activity
     * @param bundle 用于传值的bundle
     */
    public void turnActivity(Class<? extends Activity> cls,Bundle bundle){
        Intent intent = new Intent(getContext(),cls);
        if(bundle != null){
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 快捷启动一个Activity并且获取返回结果
     * @param cls 将启动的Activity
     * @param requestCode 请求码
     */
    public void turnActivityForResult(Class<? extends Activity> cls,int requestCode){
        turnActivityForResult(cls,requestCode,null);
    }

    /**
     * 快捷启动一个Activity并且获取返回结果
     * @param cls 将启动的Activity
     * @param requestCode 请求码
     * @param bundle 用于传值的bundle
     */
    public void turnActivityForResult(Class<? extends Activity> cls,int requestCode,Bundle bundle){
        Intent intent = new Intent(getContext(),cls);
        if(bundle != null){
            intent.putExtras(bundle);
        }
        startActivityForResult(intent,requestCode);
    }

    /**
     * 显示进度条
     */
    public void loading(){
        loading(getString(R.string.fastlib_loading_hint));
    }

    /**
     * 显示无限进度
     * @param hint 进度提示
     */
    public void loading(final String hint) {
        if(mLoading==null){
            mLoading = new LoadingDialog();
            mLoading.show(getChildFragmentManager(),true);
            mLoading.setOnLoadingStateListener(new LoadingDialog.OnLoadingStateListener() {
                @Override
                public void onLoadingDialogDismiss() {
                    mLoading = null;
                }
            });
        }
        if(getActivity()!=null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoading.setHint(hint);
                }
            });
        }
    }

    /**
     * 关闭进度条
     */
    public void dismissLoading(){
        if(mLoading!=null){
            mLoading.dismiss();
            mLoading=null;
        }
    }

    /**
     * 从宿主Activity中移除自身
     */
    protected void finish(){
        getFragmentManager()
                .beginTransaction()
                .remove(this)
                .commit();
    }

    /**
     * 从宿主Activity中移除自身并且加入指定Fragment
     * @param fragment 替换自身位置的新Fragment
     */
    protected void replce(Fragment fragment){
        getFragmentManager()
                .beginTransaction()
                .replace(getId(),fragment)
                .commit();
    }

    private synchronized void prepareTask(){
        if(--mPreparedTaskRemain<=0)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alreadyPrepared();
                }
            });
    }
}