package com.fastlib.net.upload;

import java.util.List;

/**
 * Created by sgfb on 2020\04\03.
 * Modified by liuwp on 2021\11\30.
 * 上传监视器代理.
 */
public class UploadMonitorProxy implements UploadMonitor{
    private static UploadMonitor sEmptyMonitor = new UploadMonitor(){
        @Override
        public void uploading(String key,long wroteSize,long rawSize){
            //uploading将会被大量回调所以保持一个空回调，减少空判断
        }
    };

    private long mWroteSize;
    private long mCurrSize;
    private long mCurrWrote;
    private String mCurrKey;
    private UploadMonitor mRealMonitor;
    private UploadMonitor mMonitor = sEmptyMonitor;
    private List<ValuePosition> mValuePositions;

    public UploadMonitorProxy(){
    }

    public UploadMonitorProxy(UploadMonitor monitor,List<ValuePosition> valuePositions){
        if(monitor != null && valuePositions != null && !valuePositions.isEmpty()){
            mRealMonitor = monitor;
            mValuePositions = valuePositions;
        }
    }

    public void wrote(long length){
        mCurrWrote += length;
        long wrote = Math.min(mCurrWrote,mCurrSize);
        uploading(mCurrKey,wrote,mCurrSize);
    }

    public void startWriteStream(long streamSize){
        if(mValuePositions != null && !mValuePositions.isEmpty()){
            ValuePosition first = mValuePositions.get(0);
            if(mWroteSize >= first.start && mWroteSize <= first.start + first.length){
                mCurrWrote = 0;
                mCurrKey = first.key;
                mValuePositions.remove(0);
                mMonitor = mRealMonitor != null ? mRealMonitor : sEmptyMonitor;
            }else
                mMonitor = sEmptyMonitor;
        }else
            mMonitor = sEmptyMonitor;
        mCurrSize = streamSize;
        mWroteSize += streamSize;
    }

    @Override
    public void uploading(String key,long wroteSize,long rawSize){
        mMonitor.uploading(key,wroteSize,rawSize);
    }
}
