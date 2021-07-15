package com.fastlib.net.download;

import com.fastlib.base.ThreadPoolManager;

import java.io.File;

/**
 * Created by sgfb on 2020\02\24.
 * 监控下载速度等状态
 */
public abstract class DownloadMonitor{
    private static final long DEFAULT_INTERVAL=1000;
    private boolean isRunning;
    private long mIntervalMilli=DEFAULT_INTERVAL;
    private long mTimer;
    private long mLastFileSize;
    protected long mExpectDownloadSize;
    protected File mFile;

    public DownloadMonitor() {

    }

    public DownloadMonitor(long intervalMilli){
        mIntervalMilli=intervalMilli;
    }

    /**
     * 下载回调
     * @param downloadedOneInterval     一次间隔下载的量
     */
    protected abstract void onDownloading(long downloadedOneInterval);

    /**
     * 已下载量
     * @return  已下载字节数
     */
    protected abstract long downloadedSize();

    /**
     * 被动监控
     */
    public void toggle(){
        if(System.currentTimeMillis()>(mTimer+mIntervalMilli)){
            mTimer=System.currentTimeMillis();
            updateDownloadStatus();
        }
    }

    private void updateDownloadStatus(){
        long downloadSize=downloadedSize();
        long downloadDiff=downloadSize-mLastFileSize;
        mLastFileSize=downloadSize;

        if(downloadDiff<0) downloadDiff=0;
        onDownloading(downloadDiff);
    }

    /**
     * 主动监控开始
     */
    public void start(){
        if(isRunning) return;
        isRunning=true;
        ThreadPoolManager.sSlowPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while(isRunning){
                        updateDownloadStatus();
                        Thread.sleep(mIntervalMilli);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 主动监控结束
     */
    public void stop(){
        isRunning=false;
        updateDownloadStatus();
    }

    public void setIntervalMilli(long millisecond){
        if(millisecond<=0) throw new IllegalArgumentException("间隔时间必须大于0");
        mIntervalMilli=millisecond;
    }

    public void setFile(File file){
        mFile=file;
    }

    public void setExpectDownloadSize(long expectDownloadSize){
        mExpectDownloadSize=expectDownloadSize;
    }
}
