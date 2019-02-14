package com.fastlib.bean;

import com.fastlib.annotation.Database;

/**
 * 包裹从网络中获取的缓存.这个缓存将会经过数据库
 */
public class RemoteCache{
    @Database(keyPrimary = true)
    public String cacheName;
    public String cache; //缓存数据会被转换成json，以便存储和传输
    public long expiry; //失效时间,毫秒值
}