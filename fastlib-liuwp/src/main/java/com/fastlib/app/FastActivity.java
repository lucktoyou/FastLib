package com.fastlib.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.fastlib.R;
import com.fastlib.annotation.ContentView;
import com.fastlib.net.Request;
import com.fastlib.utils.ImageUtil;
import com.fastlib.utils.LocalDataInject;
import com.fastlib.utils.N;
import com.fastlib.utils.StatusBarUtil;
import com.fastlib.utils.ViewInject;
import com.fastlib.utils.permission.PermissionCallback;
import com.fastlib.utils.permission.RunTimePermissionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 16/9/5.
 * Activity基本封装.
 * 1.ContentView注解，Bind视图注解.
 * 2.全局事件注册和解注册(EventObserver)
 * 3.线程池(mThreadPool)
 * 4.本地数据辅助（LocalData）
 * 5.相机相册调取（openAlbum(PhotoResultListener)和openCamera(PhotoResultListener))
 * 6.6.0权限获取辅助(RunTimePermissionUtil)
 * 7.进度提示框(LoadingDialog)
 * 8.快捷启动新页面（turnActivity、turnActivityForResult）
 */
public abstract class FastActivity extends AppCompatActivity{
    private static final int THREAD_POOL_SIZE =Runtime.getRuntime().availableProcessors()/2+1;

    protected ThreadPoolExecutor mThreadPool;
    protected volatile int mPreparedTaskRemain=3; //剩余初始化异步任务，当初始化异步任务全部结束时调用alreadyPrepared

    private PermissionCallback mPermissionCallback;
    private String mPermissionDescription;
    private boolean isGatingPhoto; //是否正在获取图像
    private PhotoResultListener mPhotoResultListener;
    private LocalDataInject mLocalDataInject;
    private LoadingDialog mLoading;
    private List<Request> mRequests = new ArrayList<>();

    protected abstract void alreadyPrepared(); //所有初始化任务结束

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mLocalDataInject=new LocalDataInject(this);
        mThreadPool=generateThreadPool();
        checkContentViewInject();
        mThreadPool.execute(new Runnable(){
            @Override
            public void run() {
                EventObserver.getInstance().subscribe(FastActivity.this,FastActivity.this);
                prepareTask();
            }
        });
    }

    /**
     * 后期绑定mThreadPool增加灵活性
     * @return 线程池
     */
    protected ThreadPoolExecutor generateThreadPool(){
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * ContentView注入，如果存在的话
     */
    protected void checkContentViewInject(){
        ContentView cv=getClass().getAnnotation(ContentView.class);
        if(cv!=null)
            setContentView(cv.value());
    }

    /**
     * 启动网络请求
     * @param request 网络请求
     */
    protected void net(Request request) {
        if (!mRequests.contains(request))
            mRequests.add(request);
        request.setHost(this).setExecutor(mThreadPool).start(false);
    }

    /**
     * 增加网络请求到列表中，但是不立即请求
     * @param request 网络请求
     */
    public void addRequest(Request request) {
        if (!mRequests.contains(request))
            mRequests.add(request);
    }

    /**
     * 开启获取相册照片
     * @param photoResultListener 取相册中相片回调
     */
    public void openAlbum(final PhotoResultListener photoResultListener){
      RunTimePermissionUtil.onStorage(this, new PermissionCallback() {
          @Override
          public void onPermissionSuccess() {
              isGatingPhoto = true;
              mPhotoResultListener = photoResultListener;
              ImageUtil.openAlbum(FastActivity.this);
          }

          @Override
          public void onPermissionFailure(String hint) {
              N.showShort(FastActivity.this,hint);
          }
      });
    }

    /**
     * 开启相机获取照片并且指定存储位置
     * @param photoResultListener 照相成功后回调
     * @param file 指定图片存储文件
     */
    public void openCamera(final PhotoResultListener photoResultListener, final File file) {
        RunTimePermissionUtil.onCamera(this, new PermissionCallback() {
            @Override
            public void onPermissionSuccess() {
                isGatingPhoto = true;
                mPhotoResultListener = photoResultListener;
                if (file == null)
                    ImageUtil.openCamera(FastActivity.this);
                else
                    ImageUtil.openCamera(FastActivity.this,file);
            }

            @Override
            public void onPermissionFailure(String hint) {
                N.showShort(FastActivity.this,hint);
            }
        });
    }

    /**
     * 开启相机获取照片
     * @param photoResultListener 拍照成功回调
     */
    protected void openCamera(PhotoResultListener photoResultListener) {
        openCamera(photoResultListener, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLocalDataInject.injectChildBack(data);
        if (isGatingPhoto) {
            isGatingPhoto = false;
            Uri photoUri = ImageUtil.getImageFromActive(requestCode, resultCode, data);
            if(photoUri != null){
                String photoPath = ImageUtil.getImageAbsolutePath(this,photoUri);
                if (mPhotoResultListener != null)
                    mPhotoResultListener.onPhotoResult(photoPath);
            }
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RunTimePermissionUtil.PERMISSION_REQUEST_CODE){
            if (RunTimePermissionUtil.verifyPermissions(grantResults)) {
                if (mPermissionCallback != null) {
                    mPermissionCallback.onPermissionSuccess();
                    mPermissionCallback = null;
                }
            } else {
                if (mPermissionCallback != null) {
                    mPermissionCallback.onPermissionFailure(mPermissionDescription + "失败");
                    mPermissionCallback = null;
                }
                mPermissionDescription = null;
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Android M运行时权限请求封装
     * @param permissionDes 权限描述
     * @param runnable 请求权限回调
     * @param permissions 请求的权限（数组类型），直接从Manifest中读取相应的值，比如Manifest.permission.WRITE_CONTACTS
     */
    public void performCodeWithPermission(@NonNull Activity activity, @NonNull String permissionDes, PermissionCallback runnable, @NonNull String... permissions){
        if(permissions == null || permissions.length == 0) return;
        mPermissionCallback = runnable;
        mPermissionDescription = permissionDes;
        if((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || RunTimePermissionUtil.checkPermissionGranted(activity, permissions)){
            if(runnable!=null){
                runnable.onPermissionSuccess();
            }
        }else{
            //permission has not been granted.
            RunTimePermissionUtil.requestPermission(activity, permissionDes, RunTimePermissionUtil.PERMISSION_REQUEST_CODE, permissions);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        afterSetContentView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        afterSetContentView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        afterSetContentView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventObserver.getInstance().unsubscribe(this,this);
        mThreadPool.shutdownNow();
        mThreadPool.purge();
        mThreadPool=null;
        for (Request request : mRequests)
            request.clear();
        mRequests.clear();
        mRequests=null;
    }

    /**
     * 在设置布局后做几个必要动作
     */
    protected void afterSetContentView(){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ViewInject.inject(FastActivity.this,findViewById(android.R.id.content),mThreadPool);
                prepareTask();
            }
        });
        mThreadPool.execute(new Runnable() {
            @Override
            public void run(){
                mLocalDataInject.localDataInject();
                prepareTask();
            }
        });
        changeStatusBarTextImgBgColor();
    }

    //设置状态栏字体、图标和背景的颜色
    private void changeStatusBarTextImgBgColor() {
        //状态栏中的文字颜色和图标颜色，需要android系统6.0以上，而且目前只有一种可以修改（一种是深色，一种是浅色即白色）
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            StatusBarUtil.setColor(FastActivity.this, getColor(R.color.pp_white),0);
        }else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            StatusBarUtil.setColor(FastActivity.this, getResources().getColor(R.color.pp_white));
        }
    }

//    //设置点击EditView以外区域可隐藏软件盘
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        return SortKeyboardUtil.setEditViewOutsideHideKeyboard(this, ev,false) || super.dispatchTouchEvent(ev);
//    }

    /**
     * 快捷启动一个Activity
     * @param cls 将启动的Activity
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
        Intent intent = new Intent(this,cls);
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
        Intent intent = new Intent(this,cls);
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
            mLoading.show(getSupportFragmentManager(),true);
            mLoading.setOnLoadingStateListener(new LoadingDialog.OnLoadingStateListener() {
                @Override
                public void onLoadingDialogDismiss() {
                    mLoading = null;
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoading.setHint(hint);
            }
        });
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
     * 其中一个异步任务完成
     */
    protected synchronized void prepareTask(){
        if(--mPreparedTaskRemain<=0)
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    alreadyPrepared();
                }
            });
    }
}