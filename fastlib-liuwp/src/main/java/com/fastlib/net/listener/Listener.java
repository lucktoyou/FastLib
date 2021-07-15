package com.fastlib.net.listener;

import com.fastlib.annotation.NetCallback;
import com.fastlib.net.Request;

/**
 * Created by sgfb on 2019/12/10
 * E-mail:602687446@qq.com
 * 网络请求回调
 */
@NetCallback("onResponseSuccess")
public interface Listener<T>{

    /**
     * 原始字节流数据回调
     * @param request 网络请求
     * @param  data 源字节
     */
    void onRawCallback(Request request, byte[] data);

    /**
     * 数据指定类型回调
     * @param request 网络请求
     * @param result 返回的实体
     */
    void onResponseSuccess(Request request, T result);

    /**
     * 错误回调
     * @param request 网络请求
     * @param error 错误信息
     */
    void onError(Request request, Exception error);
}
