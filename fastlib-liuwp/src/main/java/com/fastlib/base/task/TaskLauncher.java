package com.fastlib.base.task;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.fastlib.base.module.ModuleLife;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 17/9/6.
 * Modified by liuwp on 2020/8/11
 * 启动线性Task必要的启动器
 */
public class TaskLauncher {
    private ModuleLife mHostLife;
    private ThreadPoolExecutor mThreadPool;
    private NoReturnAction<Throwable> mExceptionHandler;
    private EmptyAction mCompleteAction;
    private boolean mForceMainThreadFlag;

    private Handler mHandle;
    private Executor mChildThreadExecutor;
    private Executor mMainThreadExecutor;


    private TaskLauncher() {
        mHandle = new Handler(Looper.getMainLooper());
        mChildThreadExecutor = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                mThreadPool.execute(command);
            }
        };
        mMainThreadExecutor = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                mHandle.post(command);
            }
        };
    }

    /**
     * 线性任务线程调度
     *
     * @param task
     */
    private void threadDispatch(final Task task) {
        if (mForceMainThreadFlag) {
            try {
                processTask(task);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            mChildThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (task.getDelay() > 0)
                            Thread.sleep(task.getDelay());
                        if (task.getOnWhichThread() == ThreadType.MAIN)
                            mMainThreadExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    processTask(task);
                                }
                            });
                        else processTask(task);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 线性任务处理具体任务
     *
     * @param task
     */
    private void processTask(Task task) {
        try {
            task.process(); //执行事件后才有返回
            if (checkStopStatus(task)) { //中断任务事件
                runLastAction();
                return;
            }
            Object obj = task.getReturn();
            Task nextTask = task.getNext();
            //过滤任务处理
            if (task.isFilterTask()) {
                if (obj != null && (obj instanceof Boolean)) {
                    Boolean b = (Boolean) obj;
                    if (!b) {
                        if (task.isCycleEnd()) {
                            while (nextTask != null && !nextTask.isAgainTask())
                                nextTask = nextTask.getNext();
                        } else
                            nextTask = task.getCycler();
                    }
                }
                obj = task.getParam(); //过滤任务的参数就是返回，递交给下一个任务
            }
            if (nextTask != null) {
                nextTask.setParam(obj);
                threadDispatch(nextTask);
            } else {
                runLastAction();
            }
        } catch (Throwable throwable) {
            //优先处理任务中存在的异常处理器，如果没有再尝试运行全局异常处理器
            boolean handled = false;
            if (task != null) {
                NoReturnAction<Throwable> taskExceptionHandler = task.getTaskExceptionHandler();
                if (taskExceptionHandler != null) {
                    taskExceptionHandler.execute(throwable);
                    handled = true;
                }
            }
            if (!handled) runExceptionHandler(throwable);
            runLastAction();
        }
    }

    private void runExceptionHandler(final Throwable throwable) {
        if (mExceptionHandler != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mExceptionHandler.execute(throwable);
                }
            };
            if (mExceptionHandler.getThreadType() == ThreadType.MAIN)
                mMainThreadExecutor.execute(runnable);
            else mChildThreadExecutor.execute(runnable);
        }
    }

    private void runLastAction() {
        if (mCompleteAction != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mCompleteAction.executeAdapt();
                }
            };
            if (mCompleteAction.getThreadType() == ThreadType.MAIN)
                mMainThreadExecutor.execute(runnable);
            else mChildThreadExecutor.execute(runnable);
        }
    }

    private boolean checkStopStatus(Task task) {
        return mHostLife.flag == ModuleLife.LIFE_DESTROYED || task.isStopNow();
    }

    /**
     * 开始线性任务
     *
     * @param task
     */
    public void startTask(Task task) {
        Task firstTask = task;
        firstTask.clean();
        while (firstTask.getPrevious() != null)
            firstTask = firstTask.getPrevious();
        threadDispatch(firstTask);
    }

    public static class Builder {
        private TaskLauncher launcher;

        public Builder(ModuleLife life, ThreadPoolExecutor threadPoolExecutor) {
            launcher = new TaskLauncher();
            launcher.mHostLife = life;
            launcher.mThreadPool = threadPoolExecutor;
        }

        /**
         * 线性任务全局异常处理,如果Task有对应异常处理器则不调用此处理器
         *
         * @param handler 异常处理器
         * @return 线性任务启动器
         */
        public Builder setExceptionHandler(NoReturnAction<Throwable> handler) {
            launcher.mExceptionHandler = handler;
            return this;
        }

        /**
         * 无论是正常流程结束还是异常结束，最后都调用这个事件，如果存在的话
         *
         * @param action 结尾任务
         * @return 线性任务启动器
         */
        public Builder setLastTask(EmptyAction action) {
            launcher.mCompleteAction = action;
            return this;
        }

        /**
         * 强制运行在主线程中，抛弃线程调度
         *
         * @param forceOnMainThread true运行在主线程中，false无限制
         * @return 线性任务启动器
         */
        public Builder setForceOnMainThread(boolean forceOnMainThread) {
            launcher.mForceMainThreadFlag = forceOnMainThread;
            return this;
        }

        public TaskLauncher build() {
            return launcher;
        }
    }
}