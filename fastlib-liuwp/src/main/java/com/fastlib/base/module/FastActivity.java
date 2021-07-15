package com.fastlib.base.module;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.fastlib.base.task.EmptyAction;
import com.fastlib.base.task.NoReturnAction;
import com.fastlib.base.task.Task;
import com.fastlib.base.task.TaskLauncher;
import com.fastlib.net.Request;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 16/9/5.
 * Modified by liuwp on 2020/3/26.
 * Activity基本封装.
 */
public abstract class FastActivity extends AppCompatActivity implements ModuleInterface {
    private ModuleDelegate mDelegate = new ModuleDelegate(this, this);
    protected ThreadPoolExecutor mThreadPool = getThreadPool();

    //----------------------------继承自Activity系列-------------------------------------//
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        created();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDelegate.onModuleHandleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mDelegate.afterSetContentView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        mDelegate.afterSetContentView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        mDelegate.afterSetContentView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed();
    }

    @Override
    public void onBackPressed() {
        boolean handled = false;
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        //越后面的fragment是越后面加入的所以需要反向触发
        for (int i = fragmentList.size() - 1; i > 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment instanceof SupportBack) {
                if (((SupportBack) fragment).onBackPressed()) {
                    handled = true;
                    break;
                }
            }
        }
        if (!handled)
            super.onBackPressed();
    }

    //----------------------------继承自Module系列-------------------------------------//
    @Override
    public void alreadyContentView(@LayoutRes int layoutId) {
        setContentView(layoutId);
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
        return findViewById(android.R.id.content);
    }

    @Override
    public ModuleLife getModuleLife() {
        return mDelegate.getModuleLife();
    }

    //----------------------------继承自ModuleLife系列-------------------------------------//
    @Override
    public void created() {
        mDelegate.created();
    }

    @Override
    public void destroyed() {
        mDelegate.destroyed();
    }
}