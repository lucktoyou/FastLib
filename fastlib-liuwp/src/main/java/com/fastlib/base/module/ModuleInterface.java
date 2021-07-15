package com.fastlib.base.module;

import android.view.View;

import androidx.annotation.LayoutRes;

import com.fastlib.base.task.EmptyAction;
import com.fastlib.base.task.NoReturnAction;
import com.fastlib.base.task.Task;
import com.fastlib.base.task.TaskLauncher;
import com.fastlib.net.Request;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * Created by sgfb on 18/7/17.
 * Modified by liuwp on 2020/3/26.
 */
public interface ModuleInterface extends ModuleLifecycle {

    void alreadyContentView(@LayoutRes int layoutId);

    void alreadyPrepared();

    void loading();

    void loading(String hint);

    void dismissLoading();

    TaskLauncher startTask(Task task);

    TaskLauncher startTask(Task task,NoReturnAction<Throwable> exceptionHandler,EmptyAction lastAction);

    void net(Request request);

    ThreadPoolExecutor getThreadPool();

    View getRootView();

    ModuleLife getModuleLife();//module是指activity或fragment.
}