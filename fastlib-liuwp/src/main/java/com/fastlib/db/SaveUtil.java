package com.fastlib.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by sgfb on 16/4/23.
 */
public class SaveUtil{
    public static final String TAG=SaveUtil.class.getSimpleName();
    public static String sSpName="default"; //存入SharedPreferences时的默认名

    private SaveUtil(){
        //can't instance
    }

    /**
     * 读取assets中的某文件.尽可能使用这个方法读取一些小的文件，并且将这个方法置于子线程中，以保持响应
     * @param am
     * @param path
     * @return 源数据
     * @throws IOException
     */
    public static byte[] loadAssetsFile(AssetManager am, String path) throws IOException {
        InputStream in=am.open(path);
        byte[] data=new byte[1024];
        int len;
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        while((len=in.read(data))!=-1&&!Thread.currentThread().isInterrupted())
            baos.write(data,0,len);
        in.close();
        return baos.toByteArray();
    }

    /**
     * 读取文件内容.尽可能使用这个方法读取一些小的文件，并且将这个方法置于子线程中，以保持响应
     * @param path
     * @return
     * @throws IOException
     */
    public static byte[] loadFile(String path)throws IOException{
        InputStream in=new FileInputStream(path);
        byte[] data=new byte[1024];
        int len;
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        while((len=in.read(data))!=-1&&!Thread.currentThread().isInterrupted())
            baos.write(data,0,len);
        in.close();
        return baos.toByteArray();
    }

    /**
     * 创建临时文件夹中的分类文件夹
     * @param context 上下文
     * @param type 取Environment中文件夹类型
     * @return
     */
    public static File getExternalTempFolder(Context context,String type){
        File root=context.getExternalCacheDir();
        File folder=new File(root,type);
        if(!folder.exists())
            folder.mkdir();
        return folder;
    }

    public static void saveToSp(Context context,String key,Object obj){
        saveToSp(context,sSpName,key,obj);
    }

    /**
     * 保存数据到SharedPreferences.仅支持基本数据
     * @param context
     * @param name
     * @param key
     * @param obj
     */
    public static void saveToSp(Context context,String name,String key,Object obj){
        SharedPreferences sp=context.getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        if(obj instanceof String)
            editor.putString(key, (String) obj);
        else if(obj instanceof Integer)
            editor.putInt(key,(int)obj);
        else if(obj instanceof Long)
            editor.putLong(key,(long)obj);
        else if(obj instanceof Float)
            editor.putFloat(key,(float)obj);
        else if(obj instanceof Double)
            editor.putFloat(key,(float)obj);
        else if(obj instanceof Boolean)
            editor.putBoolean(key,(boolean)obj);
        else
            Log.w(TAG,"can't recognised the obj type");
        editor.apply();
    }

    /**
     * 从SharedPreferences中取出数据
     * @param context
     * @param key
     * @return
     */
    public static Object getFromSp(Context context,String key){
        return getFromSp(context,sSpName,key);
    }

    /**
     * 指定sp文件名从SharedPreferences中取出数据
     * @param context
     * @param name
     * @param key
     * @return
     */
    public static Object getFromSp(Context context,String name,String key){
        SharedPreferences sp=context.getSharedPreferences(name,Context.MODE_PRIVATE);
        return sp.getAll().get(key);
    }

    /**
     * 从SharedPreferences中取出数据,如果不存在某数据返回默认数据
     * @param context
     * @param key
     * @param def
     * @return
     */
    public static <T> T getFromSp(Context context,String key,T def){
        return (T) getFromSp(context,sSpName,key,def);
    }

    /**
     * 指定sp文件名从SharedPreferences中取出数据,如果不存在某数据返回默认数据
     * @param context
     * @param name
     * @param key
     * @param def
     * @return
     */
    public static <T> T getFromSp(Context context,String name,String key,T def){
        Object obj=getFromSp(context,name,key);
        if(obj==null)
            obj=def;
        return (T) obj;
    }

    /**
     * 简单存储数据到指定文件中
     * @param file 指定文件
     * @param data 要存储的数据
     * @throws IOException
     */
    public static void saveToFile(File file,byte[] data,boolean append)throws IOException{
        OutputStream out=new FileOutputStream(file,append);
        out.write(data);
        out.close();
    }

    /**
     * 保存数据到内部
     * @param context 上下文
     * @param name 包含后缀名
     * @param data 数据
     * @param isCache 是否缓存
     * @throws IOException
     */
    public static void saveToInternal(Context context,String name,byte[] data,boolean isCache) throws IOException {
        File directory=isCache?context.getCacheDir():context.getFilesDir();
        File file=new File(directory+File.separator+name);
        file.createNewFile();
        if(!file.exists()){
            Log.w(TAG,"文件创建失败");
            return;
        }
        saveToFile(file,data,false);
    }

    /**
     * 计算缓存占用容量(内部加外部)
     * @param context
     * @return
     */
    public static long cacheSize(Context context){
        return cacheSize(context,null);
    }

    /**
     * 计算缓存占用容量(内部加外部加额外文件夹列表)
     * @param context
     * @param cacheFolders
     * @return
     */
    public static long cacheSize(Context context,File[] cacheFolders){
        File internalDir=context.getCacheDir();
        File externalDir=context.getExternalCacheDir();
        long len=fileSize(internalDir)+fileSize(externalDir);
        if(cacheFolders!=null){
            for(File f:cacheFolders)
                len+=fileSize(f);
        }
        return len;
    }

    /**
     * 文件或文件夹占用容量.应将此方法置于工作线程中
     * @param file 要查询的文件
     * @return 文件或文件夹及底下所有文件占用空间
     */
    public static long fileSize(File file){
        long count=0;
        if(file==null||!file.exists()||Thread.currentThread().isInterrupted())
            return 0;
        if(file.isFile())
            count=file.length();
        else{
            File[] files=file.listFiles();
            for(File f:files)
                count+=fileSize(f);
        }
        return count;
    }

    /**
     * 文件或文件夹占用容量.应将此方法置于工作线程中
     * @param file 要查询的文件
     * @return 文件或文件夹及底下所有文件占用空间和数量(包括根文件数)
     */
    public static FileSizeWrapper fileSizeDetail(File file){
        FileSizeWrapper wrapper=new FileSizeWrapper();
        if(file==null||!file.exists()||Thread.currentThread().isInterrupted())
            return wrapper;
        if(file.isFile()){
            wrapper.fileCount=1;
            wrapper.directionCount=0;
            wrapper.fileSizeCount=file.length();
        }
        else{
            File[] files=file.listFiles();
            for(File f:files){
                FileSizeWrapper childWrapper=fileSizeDetail(f);
                wrapper.fileCount+=childWrapper.fileCount;
                wrapper.directionCount+=childWrapper.directionCount;
                wrapper.fileSizeCount+=childWrapper.fileSizeCount;
            }
            wrapper.directionCount+=1;
        }
        return wrapper;
    }

    /**
     * 清理缓存(内部加外部)<br/>
     * 如果缓存放在其他位置,请使用clearFile(File file)
     * @param context
     */
    public static void clearCache(Context context){
        File internalDir=context.getCacheDir();
        File externalDir=context.getExternalCacheDir();
        clearFile(internalDir);
        clearFile(externalDir);
    }

    /**
     * 清理文件,如果是文件夹必须递归删除成空文件夹.应将此方法置于工作线程中
     * @param file
     * @return
     */
    public static boolean clearFile(File file){
        if(Thread.currentThread().isInterrupted())
            return false;
        if(file.isFile())
            return file.delete();
        else{
            File[] files=file.listFiles();
            for(File f:files){
                boolean b=clearFile(f);
                if(!b)
                    return b;
            }
        }
        return true;
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
            return "directionCount:"+directionCount+" fileCount:"+fileCount+" fileSizeCount:"+fileSizeCount;
        }
    }
}
