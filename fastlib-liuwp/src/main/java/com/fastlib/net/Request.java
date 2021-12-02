package com.fastlib.net;

import android.text.TextUtils;

import androidx.core.util.Pair;

import com.fastlib.annotation.NetCallback;
import com.fastlib.base.ThreadPoolManager;
import com.fastlib.net.core.RequestHeader;
import com.fastlib.net.core.ResponseHeader;
import com.fastlib.net.download.DownloadController;
import com.fastlib.net.listener.SimpleListener;
import com.fastlib.net.param.RequestParam;
import com.fastlib.net.upload.UploadMonitor;
import com.fastlib.net.tool.Statistical;
import com.fastlib.utils.core.Reflect;
import com.fastlib.utils.FastLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 2019/12/10
 * Modified by liuwp on 2021\11\30.
 * Http请求交互窗口
 */
public class Request{
    private boolean isCanceled;
    private boolean isSkipRootAddress;
    private boolean isSkipGlobalListener;
    private boolean isCallbackOnWorkThread;
    private int mConnectionTimeout;
    private int mReadTimeout;
    private String mMethod;
    private String mUrl;
    private Map<String,List<String>> mHeader = new HashMap<>();
    private RequestParam mParam = new RequestParam();
    private UploadMonitor mUploadMonitor;               //上传监视器
    private DownloadController mDownloadController;     //下载控制器(内含下载监视器)
    private SimpleListener mListener;                   //监听回调
    private Type mCustomType;                           //一个自定义回调类型，优先使用这个参数其次才是解析mListener中方法参数
    private Statistical mStatistical;
    private RequestHeader mRequestHeader;
    private ResponseHeader mResponseHeader;

    public Request(String url){
        this("GET",url);
    }

    public Request(String method,String url){
        mMethod = method;
        mUrl = url;
    }

    private void putObject(Object value){
        mParam.put(value);
    }

    private void putObject(String key,Object value){
        mParam.put(key,value);
    }

    public Request json(Map<String,Object> map){
        putObject(map);
        return this;
    }

    public Request json(List<Object> list){
        putObject(list);
        return this;
    }

    public Request put(String key,boolean value){
        putObject(key,value);
        return this;
    }

    public Request put(String key,int value){
        putObject(key,value);
        return this;
    }

    public Request put(String key,long value){
        putObject(key,value);
        return this;
    }

    public Request put(String key,float value){
        putObject(key,value);
        return this;
    }

    public Request put(String key,double value){
        putObject(key,value);
        return this;
    }

    public Request put(String key,String value){
        putObject(key,value);
        return this;
    }

    public Request put(String key,File value){
        putObject(key,value);
        return this;
    }

    public Request putHeader(String key,String value){
        List<String> values = mHeader.get(key);
        if(values == null){
            values = new ArrayList<>();
            mHeader.put(key,values);
        }else values.clear();
        values.add(value);
        return this;
    }

    private void addObject(Object value){
        mParam.add(value);
    }

    private void addObject(String key,Object value){
        mParam.add(key,value);
    }

    public Request add(String key,boolean value){
        addObject(key,value);
        return this;
    }

    public Request add(String key,int value){
        addObject(key,value);
        return this;
    }

    public Request add(String key,long value){
        addObject(key,value);
        return this;
    }

    public Request add(String key,float value){
        addObject(key,value);
        return this;
    }

    public Request add(String key,double value){
        addObject(key,value);
        return this;
    }

    public Request add(String key,String value){
        addObject(key,value);
        return this;
    }

    public Request add(String key,File value){
        addObject(key,value);
        return this;
    }

    public Request addHeader(String key,String value){
        List<String> values = mHeader.get(key);
        if(values == null){
            values = new ArrayList<>();
            mHeader.put(key,values);
        }
        values.add(value);
        return this;
    }

    public String getUrl(){
        return mUrl;
    }

    public Request setMethod(String method){
        mMethod = method;
        return this;
    }

    public String getMethod(){
        return mMethod;
    }

    public Map<String,List<String>> getHeader(){
        return mHeader;
    }

    public RequestParam getRequestParam(){
        return mParam;
    }


    public Request setListener(SimpleListener listener){
        mListener = listener;
        return this;
    }

    public SimpleListener getListener(){
        return mListener;
    }

    public Request setStatistical(Statistical statistical){
        mStatistical = statistical;
        return this;
    }

    public Statistical getStatistical(){
        return mStatistical;
    }

    public Request setRequestHeader(RequestHeader requestHeader){
        mRequestHeader = requestHeader;
        return this;
    }

    public RequestHeader getRequestHeader(){
        return mRequestHeader;
    }

    public Request setResponseHeader(ResponseHeader responseHeader){
        mResponseHeader = responseHeader;
        return this;
    }

    public ResponseHeader getResponseHeader(){
        return mResponseHeader;
    }

    public Request setDownloadController(DownloadController controller){
        mDownloadController = controller;
        return this;
    }

    public DownloadController getDownloadController(){
        return mDownloadController;
    }

    public Request setUploadMonitor(UploadMonitor monitor){
        mUploadMonitor = monitor;
        return this;
    }

    public UploadMonitor getUploadingMonitor(){
        return mUploadMonitor;
    }

    public Request setSkipRootAddress(boolean skipRootAddress){
        isSkipRootAddress = skipRootAddress;
        return this;
    }

    public boolean getSkipRootAddress(){
        return isSkipRootAddress;
    }

    public Request setSkipGlobalListener(boolean skipGlobalListener){
        isSkipGlobalListener = skipGlobalListener;
        return this;
    }

    public boolean getSkipGlobalListener(){
        return isSkipGlobalListener;
    }


    public Request setCallbackOnWorkThread(boolean callbackOnWorkThread){
        isCallbackOnWorkThread = callbackOnWorkThread;
        return this;
    }

    public boolean getCallbackOnWorkThread(){
        return isCallbackOnWorkThread;
    }

    public Request setConnectionTimeout(int connectionTimeout){
        mConnectionTimeout = connectionTimeout;
        return this;
    }

    public int getConnectionTimeout(){
        return mConnectionTimeout;
    }

    public Request setReadTimeout(int readTimeout){
        mReadTimeout = readTimeout;
        return this;
    }

    public int getReadTimeout(){
        return mReadTimeout;
    }

    public void start(){
        isCanceled = false;
        ThreadPoolManager.sSlowPool.execute(new HttpProcessor(this));
    }

    public void startSync() throws Exception{
        startSync(void.class);
    }

    public Object startSync(Type type) throws Exception{
        isCanceled = false;
        mCustomType = type;
        setCallbackOnWorkThread(true);
        HttpProcessor hp = new HttpProcessor(this);
        hp.run();
        return hp.getResultData();
    }

    public void cancel(){
        isCanceled = true;
    }

    boolean isCanceled(){
        return isCanceled;
    }

    /**
     * 如果指定结果类型为void或Void就返回null,null或Object或byte[]返回原始字节数组,String返回字符串,File则联合{@link Request#mDownloadController}来做处理,其它类型就尝试使用gson解析。
     *
     * @return 指定结果类型.
     */
    public Type getResultType(){
        if(mCustomType != null)
            return mCustomType;
        else if(mListener != null){
            NetCallback netCallback = Reflect.findAnnotation(mListener.getClass(),NetCallback.class);
            if(netCallback == null)
                throw new IllegalStateException("NetCallback annotation can't be null!");
            Method[] ms = mListener.getClass().getDeclaredMethods();
            for(Method m : ms){
                String methodFullDescription = m.toString();
                if(netCallback.value().equals(m.getName()) && !methodFullDescription.contains("volatile")){
                    Type[] paramsType = m.getGenericParameterTypes();
                    for(Type type : paramsType){
                        if(type == Request.class || type == Object.class) continue;
                        return type;
                    }
                }
            }
        }
        return null;
    }

    //============================= 以下为打印输出内容 ==============================================

    /**
     * 打印请求信息和响应结果（响应结果仅为原始字节数据new String(rawBytes)成字符串后打印）
     *
     * @param rawBytes 网络请求结束后返回的原始字节流
     */
    public void printRequestAndResponse(byte[] rawBytes){
        printRequestAndResponse(rawBytes,false);
    }

    /**
     * 打印请求信息和响应结果
     *
     * @param rawBytes  网络请求结束后返回的原始字节流
     * @param prettyLog 是否格式化日志
     */
    public void printRequestAndResponse(byte[] rawBytes,boolean prettyLog){
        StringBuilder sb = new StringBuilder();

        RequestHeader requestHeaderEntity = getRequestHeader();
        //请求行
        sb.append("【请求行】")
                .append(requestHeaderEntity.getMethod())//请求方法
                .append(' ')
                .append(requestHeaderEntity.getUrl())//url
                .append(' ')
                .append(requestHeaderEntity.getProtocol())//协议版本
                .append("\n");
        //请求头部
        for(Map.Entry<String,List<String>> requestHEntry : requestHeaderEntity.getHeaders().entrySet()){
            for(String requestHeader : requestHEntry.getValue()){
                sb.append("【请求头部】").append(requestHEntry.getKey() + ":" + requestHeader).append("\n");
            }
        }
        //请求参数
        for(Map.Entry<String,List<String>> surfaceParamEntry : getRequestParam().getSurfaceParam().entrySet()){
            for(String surfaceParamValue : surfaceParamEntry.getValue()){
                if(TextUtils.isEmpty(surfaceParamEntry.getKey())){
                    sb.append("【请求参数(SP)】").append(surfaceParamValue).append("\n");
                }else{
                    sb.append("【请求参数(SP)】").append(surfaceParamEntry.getKey() + "=" + surfaceParamValue).append("\n");
                }

            }
        }
        for(Pair<String,Object> bottomParamPair : getRequestParam().getBottomParam()){
            if(TextUtils.isEmpty(bottomParamPair.first)){
                sb.append("【请求参数(BP)】").append(bottomParamPair.second).append("\n");
            }else{
                sb.append("【请求参数(BP)】").append(bottomParamPair.first + "=" + bottomParamPair.second).append("\n");
            }
        }

        ResponseHeader rh = getResponseHeader();
        //响应行
        sb.append("【响应行】")
                .append(rh.getProtocol())//协议版本
                .append(' ')
                .append(rh.getCode()) //状态码
                .append(' ')
                .append(rh.getMessage())//状态码描述
                .append("\n");
        //响应头部
        for(Map.Entry<String,List<String>> responseHeaderEntry : rh.getHeaders().entrySet()){
            for(String responseHeader : responseHeaderEntry.getValue()){
                sb.append("【响应头部】").append(responseHeaderEntry.getKey() + ":" + responseHeader).append("\n");
            }
        }
        //响应结果
        Type resultType = getResultType();
        if(resultType == void.class || resultType == Void.class){
            sb.append("【响应结果(" + resultType.toString() + ")】\n" + null);
        }else if(resultType == null || resultType == Object.class || resultType == byte[].class){
            sb.append("【响应结果(" + (resultType == null ? null : resultType.toString()) + ")】\n").append(prettyLog ? prettyLog(rawBytes) : new String(rawBytes));
        }else if(resultType == File.class){
            sb.append("【响应结果(" + resultType.toString() + ")】\n").append(getDownloadController().getTargetFile().getAbsoluteFile());
        }else if(resultType == String.class){
            sb.append("【响应结果(" + resultType.toString() + ")】\n").append(prettyLog ? prettyLog(rawBytes) : new String(rawBytes));
        }else{
            sb.append("【响应结果(" + resultType.toString() + ")】\n").append(prettyLog ? prettyLog(rawBytes) : new String(rawBytes));
        }

        FastLog.i(sb.toString());
    }

    /**
     * 如果是有效json字符串格式化输出。
     *
     * @param bytes 字节流
     * @return 漂亮的打印日志
     */
    private String prettyLog(byte[] bytes){
        String str = new String(bytes);
        if(TextUtils.isEmpty(str)){
            return str;
        }else{
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            boolean isValidJSON;//是否是有效json字符串
            try{
                gson.fromJson(str,Object.class);
                isValidJSON = true;
            }catch(JsonSyntaxException ex){
                isValidJSON = false;
            }
            if(isValidJSON){
                JsonElement jsonElement = JsonParser.parseString(str);
                return gson.toJson(jsonElement);
            }else{
                return str;
            }
        }
    }
}
