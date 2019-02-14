package com.fastlib.bean;

import com.fastlib.net.Request;

/**
 * Created by sgfb on 16/9/20.
 * 文件下载时发送的进度广播
 */
public class EventDownloading{
    private long mMaxLength; //本次传输的所有字节数(总大小应该看第一次传数的这个值)
    private int mSpeed; //字节/秒
    private String mPath;
    private Request mRequest;

    public EventDownloading(long maxLength,int speed,String path,Request request){
        this.mMaxLength =maxLength;
        this.mSpeed =speed;
        this.mPath =path;
        this.mRequest=request;
    }

    public long getMaxLength() {
        return mMaxLength;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public String getPath() {
        return mPath;
    }

    public Request getRequest() {
        return mRequest;
    }
}
