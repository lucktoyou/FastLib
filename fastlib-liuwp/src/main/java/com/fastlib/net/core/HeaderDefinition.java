package com.fastlib.net.core;

/**
 * Created by sgfb on 2019/12/3
 * E-mail:602687446@qq.com
 * HTTP头部关键字名
 */
public interface HeaderDefinition{
    String KEY_HOST ="Host";
    String KEY_AGENT ="User-Agent";
    String KEY_ACCEPT ="Accept";
    String KEY_ACCEPT_ENCODING ="Accept_Encoding";
    String KEY_CONNECTION ="Connection";
    String KEY_CACHE_CONTROL ="Cache-Control";
    String KEY_CONTENT_DISPOSITION="content-disposition";

    /**
     * 指示资源的MIME类型
     */
    String KEY_CONTENT_TYPE ="Content-Type";

    /**
     * 内容长度这个参数在post请求且有参数时是必要的
     */
    String KEY_CONTENT_LENGTH="Content-Length";

    /**
     * 通常用于对实体内容进行压缩编码，目的是优化传输，例如用 gzip 压缩文本文件，能大幅减小体积。内容编码通常是选择性的，
     * 例如 jpg / png 这类文件一般不开启，因为图片格式已经是高度压缩过的，再压一遍没什么效果不说还浪费 CPU
     */
    String KEY_CONTENT_ENCODING="Content-Encoding";

    /**
     * 传输编码
     * 此头部与{@link #KEY_CONTENT_LENGTH}相反,在未知头部长度情况下定义编码格式
     * 最新的 HTTP 规范里，只定义了一种传输编码：分块编码（chunked）
     */
    String KEY_TRANSFER_ENCODING="Transfer-Encoding";

    /**
     * 需要将页面重新定向至的地址。一般响应码为3xx中才会出现
     */
    String KEY_LOCATION="Location";

    /**
     * 传输编码中使用的类型值.可对数据分块传输
     */
    String VALUE_TRANSFER_ENCODING_CHUNKED="chunked";

    /**
     * 不支持长连接.即完成此次http响应后应关闭socket
     */
    String VALUE_CONNECTION_CLOSE="close";

    String VALUE_CONTENT_TYPE_X_WWW_FORM_URLENCODED="application/x-www-form-urlencoded";

    String VALUE_CONTENT_TYPE_JSON="application/json";

    String VALUE_CONTENT_TYPE_MULTIPART_FORM_DATA="multipart/form-data";
}
