package com.fastlib.net.tool;

import com.fastlib.net.core.HttpTimer;

/**
 * Created by sgfb on 2020\01\05.
 * Modified by liuwp on 2021\11\30.
 */
public class StatisticalImpl implements Statistical{
    private HttpTimer mHttpTimer;
    private ContentLength mContentLength;

    public StatisticalImpl(HttpTimer mHttpTimer,ContentLength mContentLength) {
        this.mHttpTimer = mHttpTimer;
        this.mContentLength = mContentLength;
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
