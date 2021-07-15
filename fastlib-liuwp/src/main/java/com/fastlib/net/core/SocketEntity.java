package com.fastlib.net.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.Socket;

/**
 * Created by sgfb on 2019/12/9
 * Modified by liuwp on 2020/9/30.
 */
public class SocketEntity{
    private String mUrl;
    private Socket mSocket;
    private Proxy mProxy;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public SocketEntity(@NonNull String url,@NonNull Socket socket) {
        this(url,socket,null);
    }

    public SocketEntity(@NonNull String url,@NonNull Socket socket,@Nullable Proxy proxy){
        mUrl=url;
        mSocket=socket;
        mProxy=proxy;
    }

    public @NonNull String getUrl(){
        return mUrl;
    }

    public @NonNull Socket getSocket(){
        return mSocket;
    }

    public @Nullable Proxy getProxy(){
        return mProxy;
    }

    public InputStream getInputStream() throws IOException {
        if(mInputStream==null)
            mInputStream=mSocket.getInputStream();
        return mInputStream;
    }

    public OutputStream getOutputStream() throws IOException {
        if(mOutputStream==null)
            mOutputStream=mSocket.getOutputStream();
        return mOutputStream;
    }

    public void close() throws IOException {
        if(mOutputStream!=null) {
            mOutputStream.close();
            mOutputStream=null;
        }
        if(mInputStream!=null){
            mInputStream.close();
            mOutputStream=null;
        }
        mSocket.close();
        mSocket=null;
    }

    public boolean isValid() throws IOException {
        if(!mSocket.isClosed()&&!mSocket.isInputShutdown()&&!mSocket.isOutputShutdown()){
            InputStream inputStream=getInputStream();
            if(inputStream.available()<=0){
                int timeout=mSocket.getSoTimeout();
                mSocket.setSoTimeout(1);
                inputStream.mark(1);

                try{
                    int result=inputStream.read();
                    if(result==-1)
                        return false;
                }catch (InterruptedIOException e){
                    //不做任何事 异常的话说明这个socket是有效的
                }finally {
                    mSocket.setSoTimeout(timeout);
                }
            }
            return true;
        }
        return false;
    }
}
