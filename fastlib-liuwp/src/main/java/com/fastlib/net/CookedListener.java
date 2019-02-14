package com.fastlib.net;

/**
 * Created by sgfb on 17/8/14.
 * 同时返回指定的实体和被处理后的实体类
 * @param <T> 接口返回实体
 * @param <R> 处理后实体
 */
public abstract class CookedListener<T,R> implements Listener<T,Object,R>{

    public abstract void onResponseListener(Request r,T rawResult,R doneResult);

    @Override
    public void onRawData(Request r, byte[] data){
        //被适配
    }

    @Override
    public void onTranslateJson(Request r, String json) {
        //被适配
    }

    @Override
    public void onResponseListener(Request r, T result, Object result2, R lastResult) {
        onResponseListener(r,result,lastResult);
    }

    @Override
    public void onErrorListener(Request r, String error) {
        //被适配
    }
}
