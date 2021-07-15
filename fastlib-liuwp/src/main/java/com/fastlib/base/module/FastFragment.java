package com.fastlib.base.module;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fastlib.base.task.EmptyAction;
import com.fastlib.base.task.NoReturnAction;
import com.fastlib.base.task.Task;
import com.fastlib.base.task.TaskLauncher;
import com.fastlib.net.Request;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 17/1/31.
 * Modified by liuwp on 2020/3/26.
 * Fragment基本封装.
 */
public abstract class FastFragment extends Fragment implements ModuleInterface, SupportBack {
    private ModuleDelegate mDelegate = new ModuleDelegate(this, this);
    private int mContentViewLayoutId = -1;
    protected ThreadPoolExecutor mThreadPool = getThreadPool();

    //----------------------------继承自Fragment系列-------------------------------------//
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        created();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContentViewLayoutId != -1) {
            return inflater.inflate(mContentViewLayoutId, null);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.afterSetContentView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDelegate.onModuleHandleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyed();
    }

    //----------------------------继承自Module系列-------------------------------------//
    @Override
    public void alreadyContentView(@LayoutRes int layoutId) {
        mContentViewLayoutId = layoutId;
    }

    @Override
    public void loading() {
        mDelegate.loading();
    }

    @Override
    public void loading(String hint) {
        mDelegate.loading(hint);
    }

    @Override
    public void dismissLoading() {
        mDelegate.dismissLoading();
    }

    @Override
    public TaskLauncher startTask(Task task) {
        return mDelegate.startTask(task);
    }

    @Override
    public TaskLauncher startTask(Task task, NoReturnAction<Throwable> exceptionHandler, EmptyAction lastAction) {
        return mDelegate.startTask(task, exceptionHandler, lastAction);
    }

    @Override
    public void net(Request request) {
        mDelegate.net(request);
    }

    @Override
    public ThreadPoolExecutor getThreadPool() {
        return mDelegate.getThreadPool();
    }

    @Override
    public View getRootView() {
        return getView();
    }

    @Override
    public ModuleLife getModuleLife() {
        return mDelegate.getModuleLife();
    }

    //----------------------------继承自ModuleLife、SupportBack系列---------------------------//
    @Override
    public void created() {
        mDelegate.created();
    }

    @Override
    public void destroyed() {
        mDelegate.destroyed();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}