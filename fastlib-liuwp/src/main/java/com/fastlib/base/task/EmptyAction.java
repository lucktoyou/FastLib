package com.fastlib.base.task;

/**
 * Created by sgfb on 17/9/7.
 * 没有参数和返回的事件
 */
public abstract class EmptyAction extends Action<Object, Object> {

    /**
     * 适配方法，去掉了参数
     */
    protected abstract void executeAdapt();

    @Override
    protected Object execute(Object param) {
        executeAdapt();
        return null;
    }
}