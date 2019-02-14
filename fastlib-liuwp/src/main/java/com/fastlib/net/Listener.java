package com.fastlib.net;

/**
 * Created by sgfb on 16/12/28.
 * 网络回调监听.最原始级,可以使用更简便封装好的{@link SimpleListener}{@link SimpleListener2}{@link SimpleListener3}{@link CookedListener}
 */
public interface Listener<T,T2,R>{

    /**
     * 原始字节数据回调
     * @param data 源字节
     */
    void onRawData(Request r, byte[] data);

    /**
     * 数据解析成字符串时回调,这个方法运行在子线程中,可以进行一些耗时操作(在Request中可以命令返回原始字节,那么这个方法将不会被回调)
     * @param json 仅仅只是new String(data)
     */
    void onTranslateJson(Request r, String json);

    /**
     * 数据原型回调,前实体转换猜想，最多2种.
     * @param r 网络请求
     * @param result 返回的实体
     * @param result2 可能的返回实体2
     * @param cookedResult 被全局监听处理过的实体
     */
    void onResponseListener(Request r, T result, T2 result2, R cookedResult);

    /**
     * 错误回调
     * @param r 网络请求
     * @param error 简单的错误信息
     */
    void onErrorListener(Request r, String error);
}