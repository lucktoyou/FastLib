package com.fastlib.base.module;

import android.content.Intent;
import android.os.Looper;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.fastlib.R;
import com.fastlib.annotation.ContentView;
import com.fastlib.base.EventObserver;
import com.fastlib.base.LoadingDialog;
import com.fastlib.base.ThreadPoolManager;
import com.fastlib.base.task.EmptyAction;
import com.fastlib.base.task.NoReturnAction;
import com.fastlib.base.task.Task;
import com.fastlib.base.task.TaskLauncher;
import com.fastlib.base.task.ThreadType;
import com.fastlib.net.Request;
import com.fastlib.utils.core.LocalDataInject;
import com.fastlib.utils.core.Reflect;
import com.fastlib.utils.core.ViewInject;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 18/7/17.
 * Modified by liuwp on 2020/3/26.
 *
 * 1.ContentView注解，Bind视图注解.
 * 2.全局事件注册和解注册 {@link com.fastlib.base.EventObserver}
 * 3.线程池及顺序任务列辅助方法 {@link #getThreadPool()#startTask(Task)}
 * 4.本地数据辅助 {@link com.fastlib.annotation.LocalData}
 * 5.权限获取辅助 {@link com.fastlib.utils.permission.FastPermission}
 * 6.图片压缩辅助 {@link com.fastlib.utils.zipimage.FastLuban}
 */
public class ModuleDelegate implements ModuleInterface {
    private ModuleLife mLife = new ModuleLife();
    private ThreadPoolExecutor mThreadPool = ThreadPoolManager.sQuickPool;

    private LocalDataInject mLocalDataInject;
    private LoadingDialog mLoading;
    private FragmentActivity mActivity;
    private Fragment mFragment;
    private ModuleInterface mHost;

    public ModuleDelegate(ModuleInterface host, Fragment fragment) {
        mHost = host;
        mFragment = fragment;
    }

    public ModuleDelegate(ModuleInterface host, FragmentActivity activity) {
        mHost = host;
        mActivity = activity;
    }

    /**
     * ContentView注入，如果存在的话
     */
    private void checkContentViewInject() {
        ContentView cv = Reflect.findAnnotation(mHost.getClass(), ContentView.class);
        if (cv != null) {
            alreadyContentView(cv.value());
        }
    }

    /**
     * 在设置布局后视图注解和局部数据注解
     */
    protected void afterSetContentView() {
        startInternalPrepareTask();
    }

    /**
     * 内部预任务
     *
     * @return 额外的预任务
     */
    private Task genPrepareTaskList() {
        return Task.begin(new EmptyAction() {
            @Override
            protected void executeAdapt() {
                ViewInject.inject(mHost, getRootView());
                mLocalDataInject.localDataInject();
                EventObserver.getInstance().subscribe(mHost);
            }
        });
    }

    /**
     * 开始内部任务加载
     */
    private void startInternalPrepareTask() {
        Task task = genPrepareTaskList();
        NoReturnAction<Throwable> exceptionAction = new NoReturnAction<Throwable>() {
            @Override
            public void executeAdapt(Throwable param) {
                param.printStackTrace();
            }
        };
        EmptyAction lastAction = new EmptyAction() {
            @Override
            protected void executeAdapt() {
                endInternalPrepareTask();
            }
        };
        lastAction.setThreadType(ThreadType.MAIN);
        if (prepareOnMainThread()) {
            new TaskLauncher.Builder(mLife, mThreadPool)
                    .setExceptionHandler(exceptionAction)
                    .setLastTask(lastAction)
                    .setForceOnMainThread(true)
                    .build()
                    .startTask(task);
        } else {
            startTask(task, exceptionAction, lastAction);
        }
    }

    private void endInternalPrepareTask() {
        alreadyPrepared();
        mLocalDataInject.injectDelayTriggerMethod();
    }

    /**
     * 内部任务运行线程
     *
     * @return true强制在主线程中运行预任务, false无限制
     */
    protected boolean prepareOnMainThread() {
        return false;
    }

    @Override
    public void alreadyContentView(@LayoutRes int layoutId) {
        mHost.alreadyContentView(layoutId);
    }

    @Override
    public void alreadyPrepared() {
        mHost.alreadyPrepared();
    }

    public void onModuleHandleActivityResult(int requestCode, int resultCode, Intent data) {
        mLocalDataInject.injectChildBack(data);
    }

    /**
     * 显示进度条
     */
    @Override
    public void loading() {
        loading(getRealActivity().getString(R.string.loading));
    }

    /**
     * 显示无限进度条
     *
     * @param hint 进度提示
     */
    @Override
    public void loading(final String hint) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mLoading == null) {
                    mLoading = new LoadingDialog();
                    mLoading.showAllowingStateLoss(getRealActivity().getSupportFragmentManager());
                    mLoading.setOnLoadingStateListener(new LoadingDialog.OnLoadingStateListener() {
                        @Override
                        public void onLoadingDialogDismiss() {
                            mLoading = null;
                        }
                    });
                }
                mLoading.setHint(hint);
            }
        });
    }

    /**
     * 关闭进度条
     */
    @Override
    public void dismissLoading() {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mLoading != null) {
                    mLoading.dismiss();
                    mLoading = null;
                }
            }
        });
    }

    /**
     * 将某事件运行在主线程中
     *
     * @param runnable 事件
     */
    private void runOnMainThread(Runnable runnable) {
        if (Looper.getMainLooper() != Looper.myLooper())
            getRealActivity().runOnUiThread(runnable);
        else runnable.run();
    }

    private FragmentActivity getRealActivity() {
        return mActivity != null ? mActivity : mFragment.getActivity();
    }

    /**
     * 开始线性任务
     *
     * @param task 任务
     */
    @Override
    public TaskLauncher startTask(Task task) {
        return startTask(task, null, null);
    }

    /**
     * 开始线性任务，并且有异常处理和尾回调
     *
     * @param task             任务
     * @param exceptionHandler 异常处理
     * @param lastAction       尾回调
     */
    @Override
    public TaskLauncher startTask(Task task, NoReturnAction<Throwable> exceptionHandler, EmptyAction lastAction) {
        TaskLauncher taskLauncher = new TaskLauncher.Builder(mLife, mThreadPool)
                .setExceptionHandler(exceptionHandler)
                .setLastTask(lastAction)
                .build();
        taskLauncher.startTask(task);
        return taskLauncher;
    }

    /**
     * 启动网络请求
     *
     * @param request 网络请求
     */
    @Override
    public void net(Request request) {
        request.start();
    }

    @Override
    public ThreadPoolExecutor getThreadPool() {
        return mThreadPool;
    }

    @Override
    public View getRootView() {
        return mHost.getRootView();
    }

    @Override
    public ModuleLife getModuleLife() {
        return mLife;
    }

    @Override
    public void created() {
        mLife.flag = ModuleLife.LIFE_CREATED;
        mLocalDataInject = new LocalDataInject(mHost);
        checkContentViewInject();
    }

    @Override
    public void destroyed() {
        mLife.flag = ModuleLife.LIFE_DESTROYED;
        EventObserver.getInstance().unsubscribe(mHost);
    }
}