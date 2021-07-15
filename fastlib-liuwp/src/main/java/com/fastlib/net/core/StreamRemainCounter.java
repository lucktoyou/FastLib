package com.fastlib.net.core;

import java.io.IOException;

/**
 * Created by sgfb on 2019/12/5 0005
 * E-mail:602687446@qq.com
 * 流剩余计量
 */
public interface StreamRemainCounter{

    /**
     * 获取此次剩余量(这个方法可以被多次调用)
     * @return 剩余流量，字节单位
     */
    int getRemainCount() throws IOException;

    /**
     * 已读或跳过的流
     * @param readBytes 已读数
     */
    void readStream(int readBytes);
}