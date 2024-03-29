package com.fastlib.net.core;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.fastlib.net.CustomException;
import com.fastlib.net.upload.UploadMonitorProxy;
import com.fastlib.net.upload.UploadMonitor;
import com.fastlib.net.upload.ValuePosition;
import com.fastlib.net.tool.Cancelable;
import com.fastlib.net.tool.URLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by sgfb on 2019\12\03.
 * 基础实现HttpCore功能.添加便利功能可快速安全使用Http协议与服务器交互
 */
public class HttpCoreImpl extends HttpCore{
    private static UploadMonitorProxy sEmptyUploadMonitorProxy = new UploadMonitorProxy(); //默认给一个空监听,减少null判断损耗

    private int mSendBodyLength;
    private int mReceivedBodyLength;
    private long mContentLength = -1;
    private UploadMonitorProxy mUploadMonitorProxy = sEmptyUploadMonitorProxy;
    private Map<String,List<String>> mAdditionHeader = new HashMap<>();
    private List<InputStream> mPendingInputStream = new ArrayList<>();
    private String mMethod;
    private HttpOption mHttpOption = new HttpOption();
    private Cancelable mCancelable = new Cancelable(){
        @Override
        public boolean isCanceled(){
            return false;
        }
    }; //给与一个默认的Cancelable减少空判断

    public HttpCoreImpl(String url,String method){
        super(url);
        mMethod = method;
    }

    @Override
    protected Map<String,List<String>> getHeader() throws IOException{
        Map<String,List<String>> totalHeader = new HashMap<>(mAdditionHeader);
        int port = URLUtil.getPort(mUrl);
        putIfNotExist(totalHeader,HeaderDefinition.KEY_HOST,URLUtil.getHost(mUrl) + (port != 80 ? (":" + port) : ""));
        putIfNotExist(totalHeader,HeaderDefinition.KEY_ACCEPT,"*/*");
        putIfNotExist(totalHeader,HeaderDefinition.KEY_AGENT,String.format(Locale.getDefault(),"%s_%s","android",Build.VERSION.SDK));
        putIfNotExist(totalHeader,HeaderDefinition.KEY_CONNECTION,"Keep-Alive");
        putIfNotExist(totalHeader,HeaderDefinition.KEY_CACHE_CONTROL,"no-cache");
        putIfNotExist(totalHeader,HeaderDefinition.KEY_ACCEPT_ENCODING,"gzip");
        if(!totalHeader.containsKey(HeaderDefinition.KEY_TRANSFER_ENCODING)){
            if(mContentLength == -1)
                mContentLength = calContentLength();
            putIfNotExist(totalHeader,HeaderDefinition.KEY_CONTENT_LENGTH,Long.toString(mContentLength));
        }
        return totalHeader;
    }

    @Override
    protected void onSendData() throws IOException{
        byte[] buffer = new byte[8096];
        int len;
        int closeIndex = 0;
        IOException exception = null;   //使用记录exception来保证关闭所有inputStream
        OutputStream out = getSocketOutputStream();

        for(InputStream inputStream : mPendingInputStream){
            try{
                if(exception == null){
                    mUploadMonitorProxy.startWriteStream(inputStream.available());
                    while((len = inputStream.read(buffer)) != -1){
                        out.write(buffer,0,len);
                        mSendBodyLength += len;
                        mUploadMonitorProxy.wrote(len);
                        if(mCancelable.isCanceled())
                            throw new CustomException("手动取消");
                    }
                }
                inputStream.close();
                closeIndex++;
            }catch(IOException e){
                //除了第一个异常其他异常忽视掉
                if(exception == null)
                    exception = e;
            }
        }

        //关闭所有流
        while(closeIndex < mPendingInputStream.size()){
            try{
                mPendingInputStream.get(++closeIndex).close();
            }catch(Throwable t){
                //进入到这里说明已经有exception了，无视这里触发的问题
            }
        }
        if(exception != null)
            throw exception;
    }

    /**
     * 对一些必要的头部如果不存在就写入默认值
     *
     * @param map          头部群
     * @param key          必要头部键
     * @param defaultValue 必要头部值
     */
    private void putIfNotExist(Map<String,List<String>> map,String key,String defaultValue){
        if(!map.containsKey(key)){
            List<String> list = new ArrayList<>();
            list.add(defaultValue);
            map.put(key,list);
        }
    }

    /**
     * 计算发送的内容长度
     *
     * @return 内容长度
     */
    private long calContentLength() throws IOException{
        long length = 0;
        for(InputStream in : mPendingInputStream){
            length += in.available();
        }
        return length;
    }

    @Override
    protected String getRequestMethod(){
        return mMethod;
    }

    @Override
    protected HttpOption getHttpOption(){
        return mHttpOption;
    }

    @Override
    public InputStream getInputStream() throws IOException{
        return new HttpInputStream(super.getInputStream(),new StreamRemainCounter(){
            private static final int NOT_INIT = -2;

            int mRemain = NOT_INIT;

            @Override
            public int getRemainCount() throws IOException{
                if(mRemain == NOT_INIT){
                    readLengthByContentLength();
                    readLengthByChunk();
                }
                if(mRemain == 0){
                    readLengthByChunk();
                    if(mRemain == 0)
                        mRemain = -1;
                }
                if(mRemain == -1 && isConnected()){
                    end();
                }else if(mRemain == -2)
                    throw new IOException("读取不到Http内容长度");
                if(mCancelable.isCanceled())
                    throw new CustomException("手动取消");
                return mRemain;
            }

            private void readLengthByContentLength(){
                String contentLength = mResponseHeader.getHeaderFirst(HeaderDefinition.KEY_CONTENT_LENGTH);

                if(!TextUtils.isEmpty(contentLength)){
                    try{
                        mRemain = Integer.parseInt(contentLength);
                    }catch(NumberFormatException e){
                        //如果不能转换成正常数字,无视这个参数
                    }
                }
            }

            private void readLengthByChunk(){
                String transferEncoding = mResponseHeader.getHeaderFirst(HeaderDefinition.KEY_TRANSFER_ENCODING);
                if(HeaderDefinition.VALUE_TRANSFER_ENCODING_CHUNKED.equals(transferEncoding)){
                    try{
                        //可能是一个CRLF,如果是空的再尝试读一行
                        String line = readLine(HttpCoreImpl.super.getInputStream());
                        if(TextUtils.isEmpty(line)){
                            line = readLine(HttpCoreImpl.super.getInputStream());
                        }
                        mRemain = Integer.parseInt(line,16);
                    }catch(IOException | NumberFormatException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void readStream(int readBytes){
                if(readBytes == -1)
                    mRemain = -1;
                else{
                    mRemain -= readBytes;
                    mReceivedBodyLength += readBytes;
                }
            }
        },isKeepAlive());
    }

    /**
     * 提供一个流在网络连接时发送给服务器的数据.这个流的{@link InputStream#available()}必须是可用的,在使用完后会被关闭
     *
     * @param inputStream 预计发送给服务器的数据
     */
    public void addPendingInputStream(@NonNull InputStream inputStream){
        if(isBegin)
            throw new IllegalStateException("不允许在begin()后添加输入流");
        mPendingInputStream.add(inputStream);
    }

    public List<InputStream> getPendingInputStream(){
        return mPendingInputStream;
    }

    public void addHeader(String key,String value){
        if(isBegin)
            throw new IllegalStateException("不允许在begin()后设置头部参数");
        List<String> valueList = mAdditionHeader.get(key);

        if(valueList == null){
            valueList = new ArrayList<>();
            mAdditionHeader.put(key,valueList);
        }
        valueList.add(value);
    }

    public void setHeader(String key,String value){
        if(isBegin)
            throw new IllegalStateException("不允许在begin()后设置头部参数");
        List<String> valueList = mAdditionHeader.get(key);

        if(valueList == null){
            valueList = new ArrayList<>();
            mAdditionHeader.put(key,valueList);
        }else valueList.clear();
        valueList.add(value);
    }

    public int getSendHeaderLength(){
        return mSendHeaderLength;
    }

    public int getReceivedHeaderLength(){
        return mReceivedHeaderLength;
    }

    public int getSendBodyLength(){
        return mSendBodyLength;
    }

    public int getReceivedBodyLength(){
        return mReceivedBodyLength;
    }

    public void setConnectionTimeout(int connectionTimeout){
        mHttpOption.connectionTimeout = connectionTimeout;
    }

    public void setReadTimeout(int readTimeout){
        mHttpOption.readTimeout = readTimeout;
    }

    public void setCancelable(Cancelable cancelable){
        if(cancelable == null) throw new IllegalArgumentException();
        mCancelable = cancelable;
    }

    public void buildUploadMonitorProxy(UploadMonitor monitor,List<ValuePosition> valuePositions){
        if(monitor!=null && valuePositions != null && !valuePositions.isEmpty())
            mUploadMonitorProxy = new UploadMonitorProxy(monitor,valuePositions);
    }
}
