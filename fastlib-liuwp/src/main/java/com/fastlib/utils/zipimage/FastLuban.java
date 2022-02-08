package com.fastlib.utils.zipimage;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by liuwp 2020/11/23.
 * Modified by liuwp 2022/1/6.
 * <p>
 * 使用FastLuban进行图片压缩，说明：
 * ①问世背景：由于luban作者短期内不再维护，而该库图片压缩功能又比较喜欢，所以只能自行维护了。
 * ②使用方式：链式调用。
 */
public class FastLuban{
    private static final String TAG = "Luban";
    private static final String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";

    private static final int MSG_COMPRESS_START = 0;
    private static final int MSG_COMPRESS_SUCCESS = 1;
    private static final int MSG_COMPRESS_ERROR = 2;

    private Context mContext;
    private List<InputStreamProvider> mStreamProviders;
    private int mLeastCompressSize;
    private String mTargetDir;
    private OnRenameListener mRenameListener;
    private OnFilterListener mFilterListener;
    private OnCompressListener mCompressListener;

    private Handler mHandler = new Handler(Looper.getMainLooper(),new Handler.Callback(){
        @Override
        public boolean handleMessage(@NonNull Message msg){
            if(mCompressListener != null){
                switch(msg.what){
                    case MSG_COMPRESS_START:
                        mCompressListener.onStart();
                        break;
                    case MSG_COMPRESS_SUCCESS:
                        mCompressListener.onSuccess((File)msg.obj);
                        break;
                    case MSG_COMPRESS_ERROR:
                        mCompressListener.onError((Throwable)msg.obj);
                        break;
                }
            }
            return true;
        }
    });

    private FastLuban(Context context,Builder builder){
        this.mContext = context;
        this.mStreamProviders = builder.mStreamProviders;
        this.mLeastCompressSize = builder.mLeastCompressSize;
        this.mTargetDir = builder.mTargetDir;
        this.mRenameListener = builder.mRenameListener;
        this.mFilterListener = builder.mFilterListener;
    }

    public void start(OnCompressListener listener){
        this.mCompressListener = listener;
        final Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();
        while(iterator.hasNext()){
            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable(){
                @Override
                public void run(){
                    try{
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START));
                        File result = compress(mContext,iterator.next());
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS,result));
                    }catch(IOException e){
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR,e));
                    }
                }
            });
            iterator.remove();
        }
    }

    public List<File> get() throws IOException{
        List<File> results = new ArrayList<>();
        Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();
        while(iterator.hasNext()){
            results.add(compress(mContext,iterator.next()));
            iterator.remove();
        }
        return results;
    }

    public File get(final String source) throws IOException{
        return compress(mContext,new InputStreamAdapter(){
            @Override
            public InputStream openInternal() throws IOException{
                return new FileInputStream(source);
            }

            @Override
            public String getPath(){
                return source;
            }
        });
    }

    private File compress(Context context,InputStreamProvider provider) throws IOException{
        try{
            File result;
            File outFile = getImageSaveFile(context,provider);
            if(mFilterListener != null){
                if(mFilterListener.allowFileCompress(provider.getPath()) && Checker.SINGLE.needCompress(mLeastCompressSize,provider.getPath())){
                    result = new Engine(provider,outFile).compress();
                }else{
                    result = new File(provider.getPath());
                }
            }else{
                result = Checker.SINGLE.needCompress(mLeastCompressSize,provider.getPath()) ?
                        new Engine(provider,outFile).compress() :
                        new File(provider.getPath());
            }
            return result;
        }finally{
            provider.close();
        }
    }

    private File getImageSaveFile(Context context,InputStreamProvider provider) throws IOException{
        File dir = null;
        if(TextUtils.isEmpty(mTargetDir)){
            if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                dir = context.getExternalCacheDir();
            }else{
                dir = context.getCacheDir();
            }
            dir = new File(dir,DEFAULT_DISK_CACHE_DIR);
        }else{
            dir = new File(mTargetDir,DEFAULT_DISK_CACHE_DIR);
        }
        if(!dir.exists()){
            dir.mkdir();
        }
        String fileName = System.currentTimeMillis() + (int)(Math.random() * 1000) + Checker.SINGLE.extSuffix(provider);
        if(mRenameListener != null){
            fileName = mRenameListener.renameCompressFile(provider.getPath());
        }
        File outFile = new File(dir,fileName);
        if(!outFile.exists()){
            outFile.createNewFile();
        }
        return outFile;
    }

    public static Builder with(Context context){
        return new Builder(context);
    }

    public static class Builder{
        private Context mContext;
        private List<InputStreamProvider> mStreamProviders;
        private int mLeastCompressSize;
        private String mTargetDir;
        private OnRenameListener mRenameListener;
        private OnFilterListener mFilterListener;

        public Builder(Context context){
            this.mContext = context;
            this.mStreamProviders = new ArrayList<>();
            this.mLeastCompressSize = 100;
        }

        public FastLuban build(){
            return new FastLuban(mContext,this);
        }

        public Builder load(InputStreamProvider inputStreamProvider){
            mStreamProviders.add(inputStreamProvider);
            return this;
        }

        public Builder load(final File file){
            mStreamProviders.add(new InputStreamAdapter(){
                @Override
                public InputStream openInternal() throws IOException{
                    return new FileInputStream(file);
                }

                @Override
                public String getPath(){
                    return file.getAbsolutePath();
                }
            });
            return this;
        }

        public Builder load(final String source){
            mStreamProviders.add(new InputStreamAdapter(){
                @Override
                public InputStream openInternal() throws IOException{
                    return new FileInputStream(source);
                }

                @Override
                public String getPath(){
                    return source;
                }
            });
            return this;
        }

        public Builder load(final Uri uri){
            mStreamProviders.add(new InputStreamAdapter(){
                @Override
                public InputStream openInternal() throws IOException{
                    return mContext.getContentResolver().openInputStream(uri);
                }

                @Override
                public String getPath(){
                    return uri.getPath();
                }
            });
            return this;
        }

        public <T> Builder load(List<T> list){
            for(T src : list){
                if(src instanceof String){
                    load((String)src);
                }else if(src instanceof File){
                    load((File)src);
                }else if(src instanceof Uri){
                    load((Uri)src);
                }else{
                    throw new IllegalArgumentException("Incoming data type exception, it must be String or File or Uri");
                }
            }
            return this;
        }

        public Builder cache(String targetDir){
            this.mTargetDir = targetDir;
            return this;
        }

        public Builder rename(OnRenameListener renameListener){
            this.mRenameListener = renameListener;
            return this;
        }

        public Builder filter(OnFilterListener filterListener){
            this.mFilterListener = filterListener;
            return this;
        }

        /**
         * @param size 不压缩的阈值，默认100KB
         */
        public Builder ignore(int size){
            this.mLeastCompressSize = size;
            return this;
        }

        /**
         * 开始异步压缩图片
         *
         * @param listener 压缩回调
         */
        public void start(OnCompressListener listener){
            build().start(listener);
        }

        /**
         * 开始同步压缩图片
         *
         * @return 含符合压缩条件的已压缩文件或含不符合压缩条件的源文件组成的文件列表。
         */
        public List<File> get() throws IOException{
            return build().get();
        }

        /**
         * 开始同步压缩图片
         *
         * @param source 传入源文件路径,路径：①file.getAbsolutePath() ②uri.getPath().
         * @return 符合压缩条件的已压缩文件或不符合压缩条件的源文件。
         */
        public File get(String source) throws IOException{
            return build().get(source);
        }
    }
}