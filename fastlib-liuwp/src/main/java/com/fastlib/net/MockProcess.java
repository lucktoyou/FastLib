package com.fastlib.net;

/**
 * Created by sgfb on 17/2/6.
 * 规定如何返回模拟数据
 */
public interface MockProcess{
    byte[] dataResponse(Request request);
}