package com.fastlib.base.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgfb on 17/9/1.
 * rx风格任务链.现有功能是任务线性化、任务线程切换、平铺和集合任务
 */
public class Task<R> {
    private boolean isFilterTask = false; //是否是过滤任务
    private int mCycleIndex = -1; //默认往左移。如果是循环移到0,如果是跳出式任务为-2
    private long mDelay = 0;
    private Action mAction;
    private Task mPrevious;
    private Task mNext;
    private Task mCycler;
    private NoReturnAction<Throwable> mExceptionHandler; //异常处理器
    private R[] mCycleData; //循环参数
    private List mCycleResult = new ArrayList(); //循环任务返回的临时存储空间

    /**
     * 空参数开始生成任务链
     *
     * @return 任务链头部
     */
    public static Task<Object> begin() {
        return begin(new Object());
    }

    /**
     * 实体参数开始生成任务链
     *
     * @param realParam 参数
     * @param <R>       返回类型
     * @return 任务链头部
     */
    public static <R> Task<R> begin(final R realParam) {
        return begin(new Action<Object, R>() {
            @Override
            protected R execute(Object param) {
                return (R) realParam;
            }
        });
    }

    /**
     * 由事件触发的任务开始头部
     *
     * @param action 行为
     * @param <T>    参数
     * @param <R>    返回
     * @return 任务链头部
     */
    public static <T, R> Task<R> begin(Action<T, R> action) {
        return begin(action, ThreadType.WORK);
    }

    /**
     * 有事件触发的任务开始头部，指定运行线程
     *
     * @param action      行为
     * @param whichThread 运行在指定线程上
     * @param <T>         参数
     * @param <R>         返回
     * @return 任务链头部
     */
    public static <T, R> Task<R> begin(Action<T, R> action, ThreadType whichThread) {
        Task<R> task = new Task();
        task.mAction = action;
        task.mAction.setThreadType(whichThread);
        return task;
    }

    /**
     * 开始一个循环任务（平铺参数）
     *
     * @param param 被平铺参数
     * @param <T>   平铺参数类型
     * @return 任务链头部
     */
    public static <T> Task<T> beginCycle(final T... param) {
        Task task = new Task();
        task.mCycleIndex = 0;
        task.mAction = new Action<T, T[]>() {

            @Override
            protected T[] execute(T p) {
                return param;
            }
        };
        return task;
    }

    /**
     * 开始一个循环任务
     *
     * @param param 被平铺的参数
     * @param <T>   平铺参数类型
     * @return 任务链头部
     */
    public static <T> Task<T> beginCycle(final List<T> param) {
        return (Task<T>) beginCycle(param.toArray());
    }

    /**
     * 结束之前的循环任务。聚拢循环结果开始下一个任务
     *
     * @param action 聚拢之前循环结果事件
     * @param <T>    计算的事件结果
     * @return 跳出之前循环任务的事件
     */
    public <T> Task<T> again(Action<List<R>, T> action) {
        return again(action, ThreadType.WORK);
    }

    /**
     * 结束之前的循环任务。聚拢循环结果开始下一个任务
     *
     * @param action      聚拢之前循环结果事件
     * @param whichThread 指定线程
     * @param <T>         计算的事件结果
     * @return 跳出之前循环任务的事件
     */
    public <T> Task<T> again(Action<List<R>, T> action, ThreadType whichThread) {
        mNext = new Task();
        mNext.mAction = action;
        mNext.mAction.setThreadType(whichThread);
        mNext.mPrevious = this;
        mNext.mCycleIndex = -2;
        return mNext;
    }

    /**
     * 接上下一个任务
     *
     * @param action 下一个任务行为
     * @param <T>    参数
     * @return 下一个任务
     */
    public <T> Task<T> next(Action<? super R, T> action) {
        return next(action, ThreadType.WORK);
    }

    /**
     * 接上下一个任务
     *
     * @param action      下一个任务行为
     * @param whichThread 指定线程
     * @param <T>         参数
     * @return 下一个任务
     */
    public <T> Task<T> next(Action<? super R, T> action, ThreadType whichThread) {
        mNext = new Task();
        mNext.mAction = action;
        mNext.mAction.setThreadType(whichThread);
        mNext.mPrevious = this;
        //如果自身不是循环任务，但是有在循环任务之中。或者自身是循环任务，下一个任务在循环中。自身循环优先级高于非自身
        if (mCycler != null)
            mNext.mCycler = mCycler;
        if (mCycleIndex == 0)
            mNext.mCycler = this;
        return mNext;
    }

    public <T> Task<T> next(Task<T> task) {
        return next(task, ThreadType.WORK);
    }

    public <T> Task<T> next(Task<T> task, ThreadType whichThread) {
        mNext = task;
        mNext.mAction.setThreadType(whichThread);
        mNext.mPrevious = this;
        if (mCycler != null)
            mNext.mCycler = mCycler;
        if (mCycleIndex == 0)
            mNext.mCycler = this;
        return mNext;
    }

    /**
     * 过滤任务
     *
     * @param action
     * @return
     */
    public Task<R> filter(Action<R, Boolean> action) {
        return filter(action, ThreadType.WORK);
    }

    /**
     * 过滤任务，指定了运行线程
     *
     * @param action
     * @param whichThread
     * @return
     */
    public Task<R> filter(Action<R, Boolean> action, ThreadType whichThread) {
        next(action, whichThread);
        mNext.isFilterTask = true;
        return mNext;
    }

    /**
     * 循环任务，默认运行在工作线程上
     *
     * @param action 循环任务行为
     * @return 下一个任务（循环任务）
     */
    public <T> Task<T> cycle(Action<R, T[]> action) {
        return cycle(action, ThreadType.WORK);
    }

    /**
     * 循环任务
     *
     * @param action      循环任务行为
     * @param whichThread 运行在指定线程上
     * @return 下一个任务（循环任务）
     */
    public <T> Task<T> cycle(Action<R, T[]> action, ThreadType whichThread) {
        mNext = new Task();
        mNext.mAction = action;
        mNext.mPrevious = this;
        mNext.mCycleIndex = 0;
        mNext.mAction.setThreadType(whichThread);
        return mNext;
    }

    public <T> Task<T> cycleList(Action<R, List<T>> action) {
        return cycleList(action, ThreadType.WORK);
    }

    public <T> Task<T> cycleList(Action<R, List<T>> action, ThreadType whichThread) {
        mNext = new Task();
        mNext.mAction = action;
        mNext.mPrevious = this;
        mNext.mCycleIndex = 0;
        mNext.mAction.setThreadType(whichThread);
        return mNext;
    }

    /**
     * 延迟执行
     *
     * @param delay 延迟时间，ms长度
     * @return 自身
     */
    public Task<R> setDelay(long delay) {
        mDelay = delay;
        return this;
    }

    public long getDelay() {
        return mDelay;
    }

    /**
     * 行为执行完毕后的返回
     *
     * @return 指定返回
     */
    public Object getReturn() {
        Object result = null;
        if (mCycleIndex < 0) { //默认和跳出类型,不能是过滤类型
            if (mNext != null) {
                if (!isFilterTask) //如果是过滤类型，判断是否过滤数据来添加到循环返回临时空间中
                    mNext.mCycleResult.add(mAction.getReturn());
                else {
                    Boolean filtered = (Boolean) mAction.getReturn();
                    if (filtered != null && filtered)
                        mNext.mCycleResult.add(mAction.getParam());
                }
            }
            return mAction.getReturn();
        }
        if (mCycleIndex == 0 && mCycleData == null) { //循环类型，并且未生成循环数据
            Object returnData = mAction.getReturn();
            if (returnData instanceof List) mCycleData = (R[]) ((List<R>) returnData).toArray();
            else mCycleData = (R[]) returnData;
            if (mCycleData != null && mCycleData.length > 0) return mCycleData[mCycleIndex++];
            else return null;
        }
        //还是循环类型，但是已有循环数据
        if (mCycleIndex > 0) {
            if ((mCycleData == null || mCycleIndex >= mCycleData.length)) //循环终结,返回终结标识
                return null;
            else result = mCycleData[mCycleIndex++]; //返回循环中指定索引
        }
        if (mNext != null && mNext.mCycleIndex == -2) //如果下一个任务是循环跳出类型
            mNext.mCycleResult.add(result);
        return result;
    }

    public void setParam(Object obj) {
        if (mCycleIndex == -2) //如果是跳出式任务，给予循环存储数据
            mAction.setParam(mCycleResult);
        else
            mAction.setParam(obj);
    }

    /**
     * 执行行为.循环任务只执行一次拿到数组数据
     */
    public void process() throws Throwable {
        if (mCycleIndex <= 0) mAction.process();
    }

    /**
     * 获取下一个任务（可能在循环中）
     *
     * @return 下一个执行的任务
     */
    public Task getNext() {
        Task task = this;
        if (task.mCycler != null && (task.mCycler.mCycleData == null || task.mCycler.mCycleData.length == task.mCycler.mCycleIndex)) { //如果链接到一个循环器，并且循环器索引已到尾端，结束这个循环器
            return task.mNext;
        }
        if (task.mNext == null || task.mNext.mCycleIndex == -2) //如果下一个任务存在并且是跳出循环类型
            return task.mCycler != null ? task.mCycler : task.mNext;
        return task.mNext;
    }

    public void clean() {
        Task task = this;

        List<Task> list = new ArrayList<>(); //查重，重复后跳出
        list.add(task);
        while (task.mPrevious != null) {
            task = task.mPrevious;
            if (list.contains(task)) break;
            else list.add(task);
        }
        while (task != null) {
            task.mCycleData = null;
            task.mCycleResult.clear();
            if (task.mCycleIndex >= 0) task.mCycleIndex = 0;
            task = task.mNext;
            if (list.contains(task)) break;
            else list.add(task);
        }
    }

    /**
     * 是否循环尾部
     *
     * @return true循环尾部，false不是循环或者未到循环尾部
     */
    public boolean isCycleEnd() {
        return mCycler != null && mCycler.mCycleIndex >= mCycler.mCycleData.length;
    }

    public boolean isAgainTask() {
        return mCycleIndex == -2;
    }

    public Object getParam() {
        return mAction.getParam();
    }

    public boolean isStopNow() {
        return mAction.isInterrupt();
    }

    public Task getPrevious() {
        return mPrevious;
    }

    public ThreadType getOnWhichThread() {
        return mAction.getThreadType();
    }

    public boolean isFilterTask() {
        return isFilterTask;
    }

    public Task getCycler() {
        return mCycler;
    }

    public Task setTaskExceptionHandler(NoReturnAction<Throwable> handler) {
        mExceptionHandler = handler;
        return this;
    }

    public NoReturnAction<Throwable> getTaskExceptionHandler() {
        return mExceptionHandler;
    }
}