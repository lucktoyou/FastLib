package com.fastlib.net.listener;

import com.fastlib.net.Request;

/**
 * Created by sgfb on 2020\01\03.
 * 简单适配类.不需要实现{@link Listener#onRawCallback(Request, byte[])}和{@link Listener#onError(Request, Exception)}
 */
public abstract class SimpleListener<T> implements Listener<T> {

    @Override
    public void onRawCallback(Request request, byte[] data) {
        //被适配
    }

    @Override
    public void onError(Request request, Exception error) {
        //被适配
    }
}
