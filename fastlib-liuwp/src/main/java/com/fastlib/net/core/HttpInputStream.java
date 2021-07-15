package com.fastlib.net.core;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sgfb on 2019\12\04.
 * Modified by liuwp on 2020/9/30.
 * 对Http协议输入流封装
 */
public class HttpInputStream extends InputStream{
    private boolean isKeepAlive;
    private InputStream mSocketInput;
    private StreamRemainCounter mRemain;

    public HttpInputStream(InputStream socketInput,StreamRemainCounter streamRemainCounter,boolean keepAlive){
        mSocketInput = socketInput;
        mRemain =streamRemainCounter;
        isKeepAlive=keepAlive;
    }

    @Override
    public int read() throws IOException{
        int remain=mRemain.getRemainCount();
        if(remain>0) {
            int readCount = mSocketInput.read();
            mRemain.readStream(readCount);
            return readCount;
        }
        return remain;
    }

    @Override
    public int read(@NonNull byte[] b) throws IOException {
        int remain=mRemain.getRemainCount();
        if(remain>0){
            if(remain>b.length)
                remain=b.length;
            int readCount=mSocketInput.read(b,0,remain);
            mRemain.readStream(readCount);
            return readCount;
        }
        return remain;
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        int remain=mRemain.getRemainCount();
        if(remain>0) {
            int readCount=mSocketInput.read(b, off, len);
            mRemain.readStream(readCount);
            return readCount;
        }
        return remain;
    }

    @Override
    public long skip(long n) throws IOException {
        int remain=mRemain.getRemainCount();
        if(remain>0) {
            long skipCount=mSocketInput.skip(n);
            mRemain.readStream((int) skipCount);
            return skipCount;
        }
        return remain;
    }

    @Override
    public int available() throws IOException {
        return mRemain.getRemainCount();
    }

    @Override
    public void close() throws IOException {
        if(!isKeepAlive)
            mSocketInput.close();
    }
}
