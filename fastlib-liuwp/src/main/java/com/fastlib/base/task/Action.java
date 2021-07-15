package com.fastlib.base.task;

/**
 * Created by sgfb on 17/9/1.
 * 任务事件
 */
public abstract class Action<P, R> {
    private boolean isInterrupt = false;
    private P mParam;
    private R mReturn;
    protected ThreadType mThreadType = ThreadType.WORK; //被告知运行在哪个线程类型上,默认工作线程

    protected abstract R execute(P param) throws Throwable;

    public void process() throws Throwable {
        mReturn = execute(mParam);
    }

    public R getReturn() {
        return mReturn;
    }

    public P getParam() {
        return mParam;
    }

    public void setParam(P param) {
        mParam = param;
    }

    public ThreadType getThreadType() {
        return mThreadType;
    }

    public void setThreadType(ThreadType threadType) {
        mThreadType = threadType;
    }

    public void stopTask() {
        isInterrupt = true;
    }

    public boolean isInterrupt() {
        return isInterrupt;
    }
}