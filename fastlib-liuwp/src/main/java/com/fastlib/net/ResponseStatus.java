package com.fastlib.net;

/**
 * Created by sgfb on 17/6/13.
 * 网络返回状态.仅在Request（网络请求）中有引用
 * 从理解上来看，有请求就有可能后返回状态，没有请求就没有返回（自然也就没有返回状态）,当ResponseStatus在Request中是null时
 * 说明请求未开始或者失败
 */
public class ResponseStatus{
    public long time; //从请求开始到回调前耗时，毫秒值
    public int code;
    public String message;

    public void clear(){
        time=0;
        code=0;
        message=null;
    }

    @Override
    public String toString(){
        return "[Status:"+code+" "+message+"  Time:"+time+"]";
    }
}