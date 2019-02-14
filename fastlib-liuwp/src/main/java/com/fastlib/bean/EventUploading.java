package com.fastlib.bean;

/**
 * Created by sgfb on 16/10/10.
 * 上传文件时发送的广播
 */
public class EventUploading{
    private int mSpeed;
    private long mSendByte; //已发送字节
    private String mPath;

    public EventUploading(int speed,long sendByte,String path){
        this.mPath=path;
        this.mSpeed=speed;
        this.mSendByte =sendByte;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public long getSendByte() {
        return mSendByte;
    }

    public String getPath() {
        return mPath;
    }
}
