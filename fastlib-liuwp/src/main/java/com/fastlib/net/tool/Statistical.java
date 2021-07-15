package com.fastlib.net.tool;

import com.fastlib.net.core.HttpTimer;

/**
 * Created by sgfb on 2020\01\04.
 * 一些网络请求和交互中的统计
 */
public interface Statistical{

    class ContentLength{
        private long mSentLength;
        private long mReceivedLength;

        public ContentLength(long sentLength, long receivedLength) {
            this.mSentLength = sentLength;
            this.mReceivedLength = receivedLength;
        }

        public long getSentLength() {
            return mSentLength;
        }

        public long getReceivedLength() {
            return mReceivedLength;
        }

        @Override
        public String toString() {
            return "ContentLength{" +
                    "mSentLength=" + mSentLength +
                    ", mReceivedLength=" + mReceivedLength +
                    '}';
        }
    }

    /**
     * 错误后重试次数
     * TODO 待实现功能
     */
    int getRetryCount();

    /**
     * 获取时间统计
     */
    HttpTimer getHttpTimer();

    /**
     * 获取内容长度
     */
    ContentLength getContentLength();
}
