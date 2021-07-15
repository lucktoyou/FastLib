package com.fastlib.net.upload;

import java.util.List;

/**
 * Created by sgfb on 2020\04\04.
 */
public class UploadListenerProxy implements UploadingListener {
    private static UploadingListener sEmptyListener= new UploadingListener() {
        @Override
        public void uploading(String key, long wrote, long count) {
            //uploading将会被大量回调所以保持一个空回调，减少空判断
        }
    };

    private long mWroteCount;
    private long mCurrCount;
    private long mCurrWrote;
    private String mCurrKey;
    private List<ValuePosition> mValuePositions;
    private UploadingListener mListener=sEmptyListener;
    private UploadingListener mRealListener;
    public UploadListenerProxy(){}

    public UploadListenerProxy(UploadingListener listener, List<ValuePosition> valuePositions){
        if(listener!=null&&valuePositions!=null&&!valuePositions.isEmpty()){
            mRealListener=listener;
            mValuePositions=valuePositions;
        }
    }

    public void wrote(long length){
        mCurrWrote+=length;
        long wrote= Math.min(mCurrWrote, mCurrCount);
        uploading(mCurrKey,wrote,mCurrCount);
    }

    public void startWriteNextStream(long count){
        if(mValuePositions!=null&&!mValuePositions.isEmpty()){
            ValuePosition first=mValuePositions.get(0);
            if(mWroteCount>=first.start&&mWroteCount<=first.start+first.length){
                mCurrWrote=0;
                mCurrKey=first.key;
                mValuePositions.remove(0);
                mListener=mRealListener!=null?mRealListener:sEmptyListener;
            }
            else mListener=sEmptyListener;
        }
        else
            mListener=sEmptyListener;

        mCurrCount=count;
        mWroteCount+=count;
    }

    @Override
    public void uploading(String key, long wrote, long count){
        mListener.uploading(key,wrote,count);
    }
}
