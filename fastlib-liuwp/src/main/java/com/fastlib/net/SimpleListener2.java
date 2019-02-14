package com.fastlib.net;

/**
 * Created by sgfb on 17/5/25.
 * 覆写双猜想实体返回
 */
public abstract class SimpleListener2<T1,T2> implements Listener<T1,T2,Object>{

    public abstract void onResponseListener(Request r, T1 result, T2 result2);

    @Override
    public void onRawData(Request r, byte[] data) {

    }

    @Override
    public void onTranslateJson(Request r, String json) {

    }

    /**
     * 覆写实体返回
     * @param r 网络请求
     * @param result 返回的实体
     * @param result2 可能的返回实体2
     * @param object2 不存在的数据
     */
    @Override
    public void onResponseListener(Request r, T1 result, T2 result2, Object object2){
        onResponseListener(r,result,result2);
    }

    @Override
    public void onErrorListener(Request r, String error) {

    }
}