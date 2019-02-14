package com.fastlib.bean;

import com.fastlib.annotation.Database;

/**
 * Created by sgfb on 16/10/11.
 * 流量记录
 */
public class NetFlow {
    public int requestCount;
    public long receiveByte;
    public long takeByte;
    @Database(keyPrimary = true)
    public long time;

    @Override
    public String toString(){
        return "Rx:"+receiveByte+" Tx:"+takeByte+" RequestCount:"+requestCount+" time:"+time;
    }
}