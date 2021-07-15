package com.fastlib.base.task;

/**
 * Created by sgfb on 17/9/7.
 * 没有参数的事件
 */
public abstract class NoParamAction<R> extends Action<Object, R> {
    /**
     * 适配方法，去掉了参数
     *
     * @return 返回指定参数
     */
    protected abstract R executeAdapt();

    @Override
    protected R execute(Object param) {
        return executeAdapt();
    }
}
