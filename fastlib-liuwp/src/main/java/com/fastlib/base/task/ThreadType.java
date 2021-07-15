package com.fastlib.base.task;

/**
 * Created by sgfb on 17/9/4.
 * 任务运行线程类型
 */
public enum ThreadType {
    MAIN, //任务运行在主线程上
    WORK //任务运行在局部子线程上
}