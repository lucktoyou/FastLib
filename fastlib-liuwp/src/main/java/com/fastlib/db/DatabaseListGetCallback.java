package com.fastlib.db;

import java.util.List;

/**
 * Created by sgfb on 17/1/12.
 */
public interface DatabaseListGetCallback<T>{
    /**
     * 回调数据库批量数据.这个回调运行在UI线程中
     * @param result
     */
    void onResult(List<T> result);
}
