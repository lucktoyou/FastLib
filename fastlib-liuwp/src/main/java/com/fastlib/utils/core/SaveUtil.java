package com.fastlib.utils.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import com.fastlib.BuildConfig;
import com.fastlib.utils.FastLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by sgfb on 16/4/23.
 * Modified by liuwp on 2021/8/16.
 */
public class SaveUtil {
    private static final String DEFAULT_SP_NAME = BuildConfig.DEFAULT_DATA_FILE_NAME;

    private SaveUtil() {
        //can't instance
    }

    /**
     * 保存数据到SharedPreferences.仅支持基本数据
     *
     * @param name
     * @param key
     * @param obj
     */
    public static void saveToSp(String name, String key, Object obj) {
        SharedPreferences sp = ContextHolder.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (obj instanceof String)
            editor.putString(key, (String) obj);
        else if (obj instanceof Integer)
            editor.putInt(key, (int) obj);
        else if (obj instanceof Long)
            editor.putLong(key, (long) obj);
        else if (obj instanceof Float)
            editor.putFloat(key, (float) obj);
        else if (obj instanceof Double)
            editor.putFloat(key, (float) obj);
        else if (obj instanceof Boolean)
            editor.putBoolean(key, (boolean) obj);
        else
            FastLog.w("can't recognised the obj type");
        editor.apply();
    }

    public static void saveToSp(String key, Object obj) {
        saveToSp(DEFAULT_SP_NAME, key, obj);
    }

    /**
     * 指定sp文件名从SharedPreferences中取出数据,如果不存在某数据返回默认数据
     *
     * @param name
     * @param key
     * @param def
     * @param <T>
     * @return
     */
    public static <T> T getFromSp(String name, String key, T def) {
        SharedPreferences sp = ContextHolder.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        Object obj = sp.getAll().get(key);
        if (obj == null)
            obj = def;
        return (T) obj;
    }

    public static <T> T getFromSp(String key, T def) {
        return (T) getFromSp(DEFAULT_SP_NAME, key, def);
    }

    /**
     * 保存数据到内部
     *
     * @param context 上下文
     * @param name    包含后缀名
     * @param data    数据
     * @param isCache 是否缓存
     */
    public static void saveToInternal(Context context, String name, byte[] data, boolean isCache) throws IOException {
        File directory = isCache ? context.getCacheDir() : context.getFilesDir();
        File file = new File(directory + File.separator + name);
        file.createNewFile();
        if (!file.exists()) {
            FastLog.w("文件创建失败");
            return;
        }
        saveToFile(file, data, false);
    }

    /**
     * 简单存储数据到指定文件中
     *
     * @param target 指定文件
     * @param data   要存储的数据
     * @param append 是否接入到文件尾部
     * @throws IOException
     */
    public static void saveToFile(File target, byte[] data, boolean append) throws IOException {
        OutputStream out = new FileOutputStream(target, append);
        out.write(data);
        out.close();
    }

    /**
     * 数据流存储到指定文件中
     *
     * @param target      存储文件
     * @param inputStream 输入流
     * @param append      是否接入到文件尾
     * @throws IOException
     */
    public static void saveToFile(File target, InputStream inputStream, boolean append) throws IOException {
        int len;
        byte[] data = new byte[4096];
        OutputStream out = new FileOutputStream(target, append);

        while ((len = inputStream.read(data)) != -1)
            out.write(data, 0, len);
        out.close();
        inputStream.close();
    }

    /**
     * 读取文件内容.尽可能使用这个方法读取一些小的文件，并且将这个方法置于子线程中，以保持响应
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static byte[] loadFile(String path) throws IOException {
        InputStream in = new FileInputStream(path);
        byte[] data = new byte[1024];
        int len;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((len = in.read(data)) != -1 && !Thread.currentThread().isInterrupted())
            baos.write(data, 0, len);
        in.close();
        return baos.toByteArray();
    }

    /**
     * 读取assets中的某文件.尽可能使用这个方法读取一些小的文件，并且将这个方法置于子线程中，以保持响应
     *
     * @param am
     * @param path
     * @return 源数据
     * @throws IOException
     */
    public static byte[] loadAssetsFile(AssetManager am, String path) throws IOException {
        InputStream in = am.open(path);
        byte[] data = new byte[1024];
        int len;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((len = in.read(data)) != -1 && !Thread.currentThread().isInterrupted())
            baos.write(data, 0, len);
        in.close();
        return baos.toByteArray();
    }

    /**
     * 读取流
     *
     * @param in
     * @param closeEnd
     * @return
     * @throws IOException
     */
    public static byte[] loadInputStream(InputStream in, boolean closeEnd) throws IOException {
        byte[] buffer = new byte[8096];
        int len;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while ((len = in.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        if (closeEnd)
            in.close();
        return baos.toByteArray();
    }


    /**
     * 清理缓存(内部加外部)
     * 如果缓存放在其他位置,请使用clearFile(File file)
     *
     * @param context
     */
    public static void clearCache(Context context) {
        File internalDir = context.getCacheDir();
        File externalDir = context.getExternalCacheDir();
        clearFile(internalDir);
        clearFile(externalDir);
    }

    /**
     * 清理文件,如果是文件夹必须递归删除成空文件夹.应将此方法置于工作线程中
     *
     * @param file 指定删除的文件或文件夹
     * @return true删除成功 false删除失败（虽然删除目标文件夹失败但可能文件夹内部分是删除成功的）
     */
    public static boolean clearFile(File file) {
        if (Thread.currentThread().isInterrupted())
            return false;
        if (file.isFile())
            return file.delete();
        else {
            File[] files = file.listFiles();
            for (File f : files) {
                boolean b = clearFile(f);
                if (!b) return false;
            }
        }
        return true;
    }

    /**
     * 计算缓存占用容量(内部加外部)
     *
     * @param context 上下文
     * @return 缓存容量 字节
     */
    public static long cacheSize(Context context) {
        return cacheSize(context, null);
    }

    /**
     * 计算缓存占用容量(内部加外部加额外文件夹列表)
     *
     * @param context      上下文
     * @param cacheFolders 指定文件夹
     * @return 缓存占用容量 字节
     */
    public static long cacheSize(Context context, File[] cacheFolders) {
        File internalDir = context.getCacheDir();
        File externalDir = context.getExternalCacheDir();
        long len = fileSize(internalDir) + fileSize(externalDir);
        if (cacheFolders != null) {
            for (File f : cacheFolders)
                len += fileSize(f);
        }
        return len;
    }

    /**
     * 文件或文件夹占用容量.应将此方法置于工作线程中
     *
     * @param file 要查询的文件
     * @return 文件或文件夹及底下所有文件占用空间
     */
    public static long fileSize(File file) {
        long count = 0;
        if (file == null || !file.exists() || Thread.currentThread().isInterrupted())
            return 0;
        if (file.isFile())
            count = file.length();
        else {
            File[] files = file.listFiles();
            for (File f : files)
                count += fileSize(f);
        }
        return count;
    }

    /**
     * 文件或文件夹占用容量.应将此方法置于工作线程中
     *
     * @param file 要查询的文件
     * @return 文件或文件夹及底下所有文件占用空间和数量(包括根文件数)
     */
    public static FileSizeWrapper fileSizeDetail(File file) {
        FileSizeWrapper wrapper = new FileSizeWrapper();
        if (file == null || !file.exists() || Thread.currentThread().isInterrupted())
            return wrapper;
        if (file.isFile()) {
            wrapper.fileCount = 1;
            wrapper.directionCount = 0;
            wrapper.fileSizeCount = file.length();
        } else {
            File[] files = file.listFiles();
            for (File f : files) {
                FileSizeWrapper childWrapper = fileSizeDetail(f);
                wrapper.fileCount += childWrapper.fileCount;
                wrapper.directionCount += childWrapper.directionCount;
                wrapper.fileSizeCount += childWrapper.fileSizeCount;
            }
            wrapper.directionCount += 1;
        }
        return wrapper;
    }

    /**
     * 文件夹底下的文件和文件夹数量.总占空间
     */
    public static class FileSizeWrapper {
        public int directionCount;
        public int fileCount;
        public long fileSizeCount;

        @Override
        public String toString() {
            return "directionCount:" + directionCount + " fileCount:" + fileCount + " fileSizeCount:" + fileSizeCount;
        }
    }
}
