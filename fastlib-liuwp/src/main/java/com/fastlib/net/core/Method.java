package com.fastlib.net.core;

/**
 * Created by sgfb on 2019/12/10
 * E-mail:602687446@qq.com
 * Http Method定义
 */
public interface Method{

    /**
     * HTTP1.0
     */
    String GET="GET";
    String POST="POST";
    String HEAD="HEAD";

    /**
     * HTTP1.1
     */
    String OPTIONS="OPTIONS";
    String PUT="PUT";
    String PATCH="PATCH";
    String DELETE="DELETE";
    String TRACE="TRACE";
    String CONNECT="CONNECT";
}
