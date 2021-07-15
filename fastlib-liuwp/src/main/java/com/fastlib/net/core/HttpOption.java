package com.fastlib.net.core;

/**
 * Created by sgfb on 2019/12/6
 * E-mail:602687446@qq.com
 * Http核心可配置选项
 */
public class HttpOption{
    boolean autoRelocation =false;         //是否自动重定向
    int connectionTimeout=15000;          //连接超时(毫秒)
    int readTimeout=15000;                //读超时(毫秒)
}
