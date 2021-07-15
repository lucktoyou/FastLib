package com.fastlib.utils.zipimage;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Modified by liuwp 2020/11/23.
 *
 * 使用FastLuban进行图片压缩，说明：
 *  ①问世背景：由于luban作者短期内不再维护，而该库图片压缩功能又比较喜欢，所以只能自行维护了。
 *  ②使用方式：链式调用。
 */
public class FastLuban implements Handler.Callback {
    private static final String TAG = "Luban";
    private static final String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";

    private static final int MSG_COMPRESS_SUCCESS = 0;
    private static final int MSG_COMPRESS_START = 1;
    private static final int MSG_COMPRESS_ERROR = 2;

    private String mTargetDir;
    private int mLeastCompressSize;
    private OnRenameListener mRenameListener;
    private OnCompressListener mCompressListener;
    private OnFilterListener mFilterListener;
    private List<InputStreamProvider> mStreamProviders;

    private Handler mHandler;

    private FastLuban(Builder builder) {
        this.mTargetDir = builder.mTargetDir;
        this.mRenameListener = builder.mRenameListener;
        this.mStreamProviders = builder.mStreamProviders;
        this.mCompressListener = builder.mCompressListener;
        this.mLeastCompressSize = builder.mLeastCompressSize;
        this.mFilterListener = builder.mFilterListener;
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(mCompressListener!=null){
            switch (msg.what) {
                case MSG_COMPRESS_START:
                    mCompressListener.onStart();
                    break;
                case MSG_COMPRESS_SUCCESS:
                    mCompressListener.onSuccess((File) msg.obj);
                    break;
                case MSG_COMPRESS_ERROR:
                    mCompressListener.onError((Throwable) msg.obj);
                    break;
            }
        }
        return false;
    }

    private void start(final Context context) {
        if (mStreamProviders == null || mStreamProviders.size() == 0 && mCompressListener != null) {
            mCompressListener.onError(new NullPointerException("image file cannot be null"));
        }
        final Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();
        while (iterator.hasNext()) {
            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START));
                        File result = compress(context, iterator.next());
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS, result));
                    } catch (IOException e) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR, e));
                    }
                }
            });
            iterator.remove();
        }
    }

    private List<File> get(Context context) throws IOException {
        List<File> results = new ArrayList<>();
        Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();
        while (iterator.hasNext()) {
            results.add(compress(context, iterator.next()));
            iterator.remove();
        }
        return results;
    }

    private File get(Context context,InputStreamProvider provider) throws IOException {
        return compress(context,provider);
    }

    private File compress(Context context, InputStreamProvider provider) throws IOException {
        try {
            File result;
            File outFile = getImageSaveFile(context,provider);
            if (mFilterListener != null) {
                if (mFilterListener.allowFileCompress(provider.getPath()) && Checker.SINGLE.needCompress(mLeastCompressSize, provider.getPath())) {
                    result = new Engine(provider, outFile).compress();
                } else {
                    result = new File(provider.getPath());
                }
            } else {
                result = Checker.SINGLE.needCompress(mLeastCompressSize, provider.getPath()) ?
                        new Engine(provider, outFile).compress() :
                        new File(provider.getPath());
            }
            return result;
        } finally {
            provider.close();
        }
    }

    private File getImageSaveFile(Context context, InputStreamProvider provider) {
        File dir = null;
        if (TextUtils.isEmpty(mTargetDir)) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                dir = context.getExternalCacheDir();
            } else {
                dir = context.getCacheDir();
            }
            dir = new File(dir, DEFAULT_DISK_CACHE_DIR);
        } else {
            dir = new File(mTargetDir, DEFAULT_DISK_CACHE_DIR);
        }
        if (!dir.exists()) {
            dir.mkdir();
        }
        String fileName = System.currentTimeMillis() + (int) (Math.random() * 1000) + Checker.SINGLE.extSuffix(provider);
        if (mRenameListener != null) {
            fileName = mRenameListener.renameCompressFile(provider.getPath());
        }
        File outFile = new File(dir, fileName);
        if (!outFile.exists()) {
            try {
                outFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outFile;
    }

    public static class Builder {
        private Context context;
        private String mTargetDir;
        private int mLeastCompressSize = 100;
        private OnRenameListener mRenameListener;
        private OnFilterListener mFilterListener;
        private OnCompressListener mCompressListener;
        private List<InputStreamProvider> mStreamProviders;

        Builder(Context context) {
            this.context = context;
            this.mStreamProviders = new ArrayList<>();
        }

        private FastLuban build() {
            return new FastLuban(this);
        }

        public Builder load(InputStreamProvider inputStreamProvider) {
            mStreamProviders.add(inputStreamProvider);
            return this;
        }

        public Builder load(final File file) {
            mStreamProviders.add(new InputStreamAdapter() {
                @Override
                public InputStream openInternal() throws IOException {
                    return new FileInputStream(file);
                }

                @Override
                public String getPath() {
                    return file.getAbsolutePath();
                }
            });
            return this;
        }

        public Builder load(final String source) {
            mStreamProviders.add(new InputStreamAdapter() {
                @Override
                public InputStream openInternal() throws IOException {
                    return new FileInputStream(source);
                }

                @Override
                public String getPath() {
                    return source;
                }
            });
            return this;
        }

        public Builder load(final Uri uri) {
            mStreamProviders.add(new InputStreamAdapter() {
                @Override
                public InputStream openInternal() throws IOException {
                    return context.getContentResolver().openInputStream(uri);
                }

                @Override
                public String getPath() {
                    return uri.getPath();
                }
            });
            return this;
        }

        public <T> Builder load(List<T> list) {
            for (T src : list) {
                if (src instanceof String) {
                    load((String) src);
                } else if (src instanceof File) {
                    load((File) src);
                } else if (src instanceof Uri) {
                    load((Uri) src);
                } else {
                    throw new IllegalArgumentException("Incoming data type exception, it must be String or File or Uri");
                }
            }
            return this;
        }

        public Builder targetDir(String targetDir) {
            this.mTargetDir = targetDir;
            return this;
        }

        public Builder rename(OnRenameListener renameListener) {
            this.mRenameListener = renameListener;
            return this;
        }

        public Builder filter(OnFilterListener filterListener) {
            this.mFilterListener = filterListener;
            return this;
        }

        /**
         * @param size 不压缩的阈值，默认100KB
         */
        public Builder ignore(int size) {
            this.mLeastCompressSize = size;
            return this;
        }

        /**
         * 开始异步压缩图片
         * @param listener 压缩回调
         */
        public void start(OnCompressListener listener) {
            this.mCompressListener = listener;
            build().start(context);
        }

        /**
         * 开始同步压缩图片
         * @return 含符合压缩条件的已压缩文件或含不符合压缩条件的源文件组成的文件列表。
         */
        public List<File> get() throws IOException {
            return build().get(context);
        }

        /**
         * 开始同步压缩图片
         * @param source 传入源文件路径,路径：①file.getAbsolutePath() ②uri.getPath().
         * @return 符合压缩条件的已压缩文件或不符合压缩条件的源文件。
         */
        public File get(final String source) throws IOException {
            return build().get(context,new InputStreamAdapter() {
                @Override
                public InputStream openInternal() throws IOException {
                    return new FileInputStream(source);
                }

                @Override
                public String getPath() {
                    return source;
                }
            });
        }
    }
}