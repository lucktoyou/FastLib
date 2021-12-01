package com.fastlib.net.listener;

import com.fastlib.annotation.NetCallback;
import com.fastlib.net.Request;

import java.lang.reflect.Type;

/**
 * Created by sgfb on 2019/12/10
 * Modified by liuwp on 2021\11\30.
 * 网络请求回调
 */
@NetCallback("onResponseSuccess")
public abstract class SimpleListener<T>{

    /**
     * 原始字节流数据回调
     *
     * @param request 网络请求
     * @param data    源字节
     * @param type    {@link Request#getResultType()}
     * @return 处理后的源字节,影响回调结果
     */
    public byte[] onRawData(Request request,byte[] data,Type type){
        return data;
    }

    /**
     * 数据指定类型回调
     *
     * @param request 网络请求
     * @param result  返回的实体
     */
    public abstract void onResponseSuccess(Request request,T result);

    /**
     * 错误回调
     *
     * @param request 网络请求
     * @param error   错误信息
     */
    public abstract void onError(Request request,Exception error);
}
