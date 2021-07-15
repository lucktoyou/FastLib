package com.fastlib.net.core;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by sgfb on 2019/12/9
 * E-mail:602687446@qq.com
 * 此输出流做Socket真实输出流代理.跳过close方法以遵守keep-alive重用性
 */
public class HttpOutputStream extends OutputStream {
    private boolean isKeepAlive;
    private OutputStream mRealOutputStream;

    public HttpOutputStream(OutputStream outputStream,boolean keepAlive){
        mRealOutputStream = outputStream;
        isKeepAlive=keepAlive;
    }

    @Override
    public void write(int b) throws IOException {
        mRealOutputStream.write(b);
    }

    @Override
    public void write(@NonNull byte[] b) throws IOException {
        mRealOutputStream.write(b);
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        mRealOutputStream.write(b,off,len);
    }

    @Override
    public void flush() throws IOException {
        mRealOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        if(!isKeepAlive)
            mRealOutputStream.close();
    }
}
