package com.fastlib.db;

/**
 * Created by sgfb on 17/1/5.
 */

public interface DatabaseGetCallback<T>{
    /**
     * 回调数据库单条数据.这个回调运行在UI线程中
     * @param data
     */
    void onResult(T data);
}