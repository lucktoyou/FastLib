package com.fastlib.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by sgfb on 16/4/25.
 * 以Socket为基础.支持断点上传和下载
 */
public abstract class RandomFileTranslate{
    protected boolean isUpload; //是否上传,如果为否就是下载
    protected boolean isTranslating;
    protected boolean isCompletion;
    protected long mSkip; //跳过数据
    protected int mPerTranslate=1024; //每次传输的数据量
    protected long mCount=0;
    protected Socket mSocket,mControlSocket;
    protected File mFile;
    private String mAddress;
    private int mPort,mControlPort;
    private OnProgressListener mListener;

    public RandomFileTranslate(File file, final String address, final int port)throws IOException{
        this(file, address, port, -1);
    }

    public RandomFileTranslate(File file,String address,int port,int controlPort)throws  IOException{
        if(!file.exists())
            throw new IOException("File not exists");
        mAddress=address;
        mPort=port;
        mControlPort=controlPort;
        mFile=file;
    }

    /**
     * 文件传输前交互,应该在这个方法里将调整upload和skip标志
     */
    public abstract void interactive();

    /**
     * 在未下载完时中断下载
     */
    public abstract void unCompletion();
    /**
     * 开始文件传输,如果一开始就错误,isTranslating应该是false
     */
    public void start(){
        if(isTranslating)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket=new Socket(mAddress,mPort);
                    if(mControlPort!=-1)
                        mControlSocket=new Socket(mAddress,mControlPort);
                    if(mSocket==null)
                        return;
                    interactive();
                    resume();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 暂停文件传输
     */
    public void pause(){
        isTranslating=false;
    }

    /**
     * 暂停后继续下载
     */
    public void resume(){
        if(mSocket==null)
            return;
        isTranslating=true;
        if(isUpload)
            new Thread(new UploadThread()).start();
        else
            new Thread(new DownloadThread()).start();
    }

    public void close(){
        isTranslating=false;
        new Thread(){
            @Override
            public void run(){
                if(!isCompletion)
                    unCompletion();
                try{
                    if(mSocket!=null&&!mSocket.isClosed())
                        mSocket.close();
                    if(mControlSocket!=null&&!mControlSocket.isConnected())
                        mControlSocket.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void setOnProgressListener(OnProgressListener l){
        mListener=l;
    }

    class UploadThread implements Runnable{

        @Override
        public void run() {
            try {
                FileInputStream in=new FileInputStream(mFile);
                OutputStream out=mSocket.getOutputStream();
                byte[] data=new byte[mPerTranslate];
                int len=0;

                in.skip(mSkip);
                while(isTranslating&&(len=in.read(data))!=-1) {
                    out.write(data, 0, len);
                    mSkip+=len;
                    mCount+=len;
                    if(mListener!=null)
                        mListener.incrementChanged(mCount);
                }
                in.close();
                if(len==-1){
                    isCompletion=true;
                    close(); //传输完成后关闭所有输入输出
                }
            } catch (FileNotFoundException e) {
                //do noting
            } catch (IOException e) {
                //do noting
            }
        }
    }
    class DownloadThread implements Runnable{

        @Override
        public void run(){
            try {
                FileOutputStream out=new FileOutputStream(mFile,true);
                InputStream in=mSocket.getInputStream();
                byte[] data=new byte[mPerTranslate];
                int len=0;

                while(isTranslating&&(len=in.read(data))!=-1) {
                    out.write(data, 0, len);
                    mCount+=len;
                    if(mListener!=null)
                        mListener.incrementChanged(mCount);
                }
                out.close();
                if(len==-1) {
                    isCompletion=true;
                    close(); //传输完成后关闭所有输入输出
                }
            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public interface OnProgressListener{
        void incrementChanged(long count);
    }
}