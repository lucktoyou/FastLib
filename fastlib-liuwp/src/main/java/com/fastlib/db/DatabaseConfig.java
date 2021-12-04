package com.fastlib.db;

import android.os.Environment;

import androidx.annotation.NonNull;

import com.fastlib.BuildConfig;
import com.fastlib.utils.FastLog;
import com.fastlib.utils.core.ContextHolder;
import com.fastlib.utils.core.SaveUtil;

import java.io.File;


/**
 * Created by liuwp on 2020/4/20.
 * 默认数据库配置
 */
public class DatabaseConfig{
    private static final String TAG = DatabaseConfig.class.getSimpleName();
    private static final String SAVE_INT_DB_VERSION = "dbVersion";
    private static final String DEFAULT_DATABASE_NAME = BuildConfig.DEFAULT_DATA_FILE_NAME;
    private static DatabaseConfig instance;
    private String mCurrentDatabase;
    private int mVersion;
    private final File mFileRefDir; //数据库引用外部文件根目录

    public synchronized static DatabaseConfig getInstance(){
        if(instance == null){
            instance = new DatabaseConfig();
            FastLog.d(TAG + "类实例化");
        }
        return instance;
    }

    private DatabaseConfig(){
        mCurrentDatabase = DEFAULT_DATABASE_NAME;
        mVersion = SaveUtil.getFromSp(SAVE_INT_DB_VERSION,1);
        mFileRefDir = initDatabaseFileReferenceDirectory();
    }

    public void switchDatabase(@NonNull String databaseName){
        mCurrentDatabase = databaseName;
    }

    public String getDatabaseName(){
        return mCurrentDatabase;
    }

    public String getDatabaseNameComplete(){
        return mCurrentDatabase + ".db";
    }

    public void setVersion(int newVersion){
        if(newVersion <= mVersion){
            return;
        }
        mVersion = newVersion;
        SaveUtil.saveToSp(SAVE_INT_DB_VERSION,newVersion);
    }

    public int getVersion(){
        return mVersion;
    }

    private File initDatabaseFileReferenceDirectory(){
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

    public File getFileRefDir(){
        return mFileRefDir;
    }

    public String getDefaultDatabaseName(){
        return DEFAULT_DATABASE_NAME;
    }
}