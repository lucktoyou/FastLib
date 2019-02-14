package com.fastlib.net;

/**
 * Created by sgfb on 17/4/26.
 * 最少可以只重载一个onResponseListener就能使用的网络回调监听
 */
public abstract class SimpleListener<T> implements Listener<T,Object,Object>{

    public abstract void onResponseListener(Request r,T result);

    @Override
    public void onRawData(Request r,byte[] data) {
        //被适配
    }

    @Override
    public void onTranslateJson(Request r,String json) {
        //被适配
    }

    @Override
    public void onErrorListener(Request r, String error){
        //被适配
    }

    /**
     * 覆写实体返回
     * @param r 网络请求
     * @param result 返回的实体
     * @param result2 不可能存在的返回实体2
     * @param result3 不可能存在的返回实体3
     */
    @Override
    public void onResponseListener(Request r, T result, Object result2, Object result3){
        onResponseListener(r,result);
    }
}