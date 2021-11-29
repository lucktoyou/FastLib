package com.fastlib.net.download;

import com.fastlib.base.ThreadPoolManager;

import java.io.File;

/**
 * Created by sgfb on 2020\02\24.
 * 监控下载速度等状态
 */
public abstract class DownloadMonitor{

    private long mIntervalMillis;
    private long mExpectDownloads;
    private File mFile;
    private boolean isRunning;
    private long mCurrentTime;
    private long mLastDoneDownloads;

    public DownloadMonitor(){
        //默认间隔毫秒数
        this.mIntervalMillis = 1000;
    }

    public DownloadMonitor(long intervalMillis){
        this.mIntervalMillis = intervalMillis;
    }

    void setExpectDownloadSize(long expectDownloads){
        this.mExpectDownloads = expectDownloads;
    }

    void setFile(File file){
        this.mFile = file;
    }

    private void updateDownloadStatus(){
        long doneDownloads = mFile == null ? 0 : mFile.length();
        long downloadsOneInterval = doneDownloads - mLastDoneDownloads;
        mLastDoneDownloads = doneDownloads;
        if(downloadsOneInterval < 0) downloadsOneInterval = 0;
        onDownloading(downloadsOneInterval,doneDownloads,mExpectDownloads,mFile);
    }

    /**
     * 下载回调
     *
     * @param downloadsOneInterval 一次间隔下载的量
     * @param doneDownloads        已下载量
     * @param expectDownloads      预计下载量
     * @param file                 存储下载数据的文件
     */
    protected abstract void onDownloading(long downloadsOneInterval,long doneDownloads,long expectDownloads,File file);

    /**
     * 被动监控
     */
    public void toggle(){
        if(System.currentTimeMillis() > (mCurrentTime + mIntervalMillis)){
            mCurrentTime = System.currentTimeMillis();
            updateDownloadStatus();
        }
    }

    /**
     * 主动监控开始
     */
    public void start(){
        if(isRunning) return;
        isRunning = true;
        ThreadPoolManager.sSlowPool.execute(new Runnable(){
            @Override
            public void run(){
                try{
                    while(isRunning){
                        updateDownloadStatus();
                        Thread.sleep(mIntervalMillis);
                    }
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 主动监控结束
     */
    public void stop(){
        isRunning = false;
        updateDownloadStatus();
    }
}
