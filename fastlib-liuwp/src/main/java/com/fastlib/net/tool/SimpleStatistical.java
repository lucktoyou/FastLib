package com.fastlib.net.tool;

import com.fastlib.net.core.HttpTimer;

/**
 * Created by sgfb on 2020\01\05.
 */
public class SimpleStatistical implements Statistical{
    private int mRetryCount;
    private HttpTimer mHttpTimer;
    private ContentLength mContentLength;

    public SimpleStatistical(int mRetryCount, HttpTimer mHttpTimer, ContentLength mContentLength) {
        this.mRetryCount = mRetryCount;
        this.mHttpTimer = mHttpTimer;
        this.mContentLength = mContentLength;
    }

    @Override
    public int getRetryCount() {
        return mRetryCount;
    }

    @Override
    public HttpTimer getHttpTimer() {
        return mHttpTimer;
    }

    @Override
    public ContentLength getContentLength() {
        return mContentLength;
    }
}
