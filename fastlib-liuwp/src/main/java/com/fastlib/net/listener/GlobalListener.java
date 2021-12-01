package com.fastlib.net.listener;

import com.fastlib.net.Request;

import java.lang.reflect.Type;

/**
 * Created by sgfb on 17/7/31.
 * Modified by liuwp on 2021\11\30.
 * 与{@link SimpleListener}相似.但是全局仅存一个且在每个请求的Listener方法之前触发.
 * 如果{@link Request}没有设置回调监听器，则只会回调onLaunchRequestBefore.
 * 允许修改回调数据.
 */
public class GlobalListener{

    /**
     * 请求即将发送前回调
     *
     * @param request 网络请求
     */
    public void onLaunchRequestBefore(Request request){

    }

    /**
     * 原始字节数据回调
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
     * 数据原型回调
     *
     * @param request 网络请求
     * @param result  返回的实体
     * @return 默认返回非空result
     */
    public Object onResponseListener(Request request,Object result){
        return result;
    }

    /**
     * 错误回调
     *
     * @param request 网络请求
     * @param error   错误异常
     * @return 被处理后的错误异常
     */
    public Exception onErrorListener(Request request,Exception error){
        return error;
    }
}