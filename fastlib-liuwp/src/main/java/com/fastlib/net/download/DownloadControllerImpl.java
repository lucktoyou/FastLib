package com.fastlib.net.download;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fastlib.utils.core.SaveUtil;
import com.fastlib.net.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sgfb on 2020\02\20.
 * Modified by liuwp on 2020\10\9.
 * Modified by liuwp on 2021\11\30.
 * 单线程下载.
 */
public class DownloadControllerImpl implements DownloadController{
    private boolean useServerFilename;
    private boolean supportAppend;//是否支持断点续传
    private File mTargetFile;
    private DownloadMonitor mMonitor;

    public DownloadControllerImpl(@NonNull File targetFile){
        this(targetFile,false,false);
    }

    public DownloadControllerImpl(@NonNull File targetFile,boolean useServerFilename,boolean supportAppend){
        this.useServerFilename = useServerFilename;
        this.supportAppend = supportAppend;
        this.mTargetFile = targetFile;
    }

    @Override
    public void prepare(Request request){
        if(supportAppend){
            long existsLength = mTargetFile.length();
            request.putHeader("Range","bytes=" + existsLength + "-");
        }
    }

    @Override
    public void onStreamReady(InputStream inputStream,@Nullable String filename,long length) throws IOException{
        if(useServerFilename && !TextUtils.isEmpty(filename)){
            File realFile = new File(mTargetFile.getParent(),filename);
            mTargetFile.renameTo(realFile);
        }
        try{
            if(mMonitor != null){
                mMonitor.setFile(mTargetFile);
                mMonitor.setExpectDownloadSize(length);
                mMonitor.start();
            }
            SaveUtil.saveToFile(mTargetFile,inputStream,supportAppend);
        }finally{
            if(mMonitor != null) mMonitor.stop();
        }
    }

    @Override
    public File getTargetFile(){
        return mTargetFile;
    }

    public void setDownloadMonitor(DownloadMonitor monitor){
        mMonitor = monitor;
    }
}
