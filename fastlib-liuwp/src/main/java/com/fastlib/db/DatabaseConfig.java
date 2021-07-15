package com.fastlib.db;

import android.os.Environment;

import androidx.annotation.NonNull;

import com.fastlib.BuildConfig;
import com.fastlib.utils.core.ContextHolder;
import com.fastlib.utils.core.SaveUtil;

import java.io.File;


/**
 * Created by liuwp on 2020/4/20.
 * 默认数据库配置
 */
public class DatabaseConfig{
    private static final String SAVE_INT_DB_VERSION = "dbVersion";
    private static final String DEFAULT_DATABASE_NAME = BuildConfig.DEFAULT_DATA_FILE_NAME;
    private int mVersion;
    private String mCurrentDatabase;
    private File mFileRefDir;

    public DatabaseConfig(){
        mVersion = SaveUtil.getFromSp(ContextHolder.getContext(),SAVE_INT_DB_VERSION,1);
        mCurrentDatabase = DEFAULT_DATABASE_NAME+".db";
        mFileRefDir = createDatabaseFileReferenceDirectory();
    }

    /**
     * @return 数据库引用外部文件根目录
     */
    private File createDatabaseFileReferenceDirectory(){
        File dir = null;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            dir = ContextHolder.getContext().getExternalFilesDir(null);
        }else{
            dir = ContextHolder.getContext().getFilesDir();
        }
        dir = new File(dir,"dbRef");
        if(!dir.exists()){
            dir.mkdir();
        }
        return dir;
    }

    public void switchDatabase(@NonNull String databaseName){
        mCurrentDatabase = databaseName+".db";
    }

    public String getDatabaseNameComplete(){
        return mCurrentDatabase;
    }

    public void setVersion(int newVersion){
        if(newVersion<=mVersion){
            return;
        }
        mVersion = newVersion;
        SaveUtil.saveToSp(ContextHolder.getContext(),SAVE_INT_DB_VERSION,newVersion);
    }

    public int getVersion(){
        return mVersion;
    }

    public File getFileRefDir(){
        return mFileRefDir;
    }

    public String getDefaultDatabaseName(){
        return DEFAULT_DATABASE_NAME;
    }
}