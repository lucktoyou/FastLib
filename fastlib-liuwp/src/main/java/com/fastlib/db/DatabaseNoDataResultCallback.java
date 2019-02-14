package com.fastlib.db;

/**
 * Created by sgfb on 17/3/7.
 * 数据库无数据调取异步回调(增删改)
 */
public interface DatabaseNoDataResultCallback {
    void onResult(boolean success);
}