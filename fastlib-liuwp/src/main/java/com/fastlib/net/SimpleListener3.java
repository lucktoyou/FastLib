package com.fastlib.net;

/**
 * Created by sgfb on 17/5/25.
 * 最少可以只重载一个onResponseListener就能使用的网络回调监听,三猜想实体
 */
public abstract class SimpleListener3<T1,T2,T3> implements Listener<T1,T2,T3>{

    @Override
    public void onRawData(Request r, byte[] data){

    }

    @Override
    public void onTranslateJson(Request r, String json) {

    }

    @Override
    public void onErrorListener(Request r, String error) {

    }
}
