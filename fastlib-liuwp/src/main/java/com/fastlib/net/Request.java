package com.fastlib.net;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.fastlib.db.FastDatabase;
import com.fastlib.db.ServerCache;
import com.google.gson.Gson;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author sgfb
 * 请求体<br/>
 * 每个任务都是不同的，（NetQueue）会根据属性来配置请求，调整请求开始完成或失败后不同的事件
 */
public class Request{
    private static final Object sLock = new Object();
    private static final int MAX_POOL_SIZE = 20; //池中最大保存数
    private static Request sPool;
    private static int sPoolSize = 0;

    private boolean isAcceptGlobalCallback; //是否接受全局回调监听.默认true
    private boolean isReplaceChinese; //是否替换中文url,默认为true
    private boolean hadRootAddress; //是否已加入根地址
    private boolean useFactory; //是否使用预设值
    private boolean isSendGzip; //指定这次请求发送时是否压缩成gzip流
    private boolean isReceiveGzip; //指定这次请求是否使用gzip解码
    private byte[] mByteStream; //原始字节流，如果这个值存在就不会发送mParams参数了.如果存在但是长度为0发送mParams参数json化数据
    private String method;
    private String mUrl;
    private List<Pair<String, String>> mSendCookies;
    private Downloadable mDownloadable;
    private Map<String,List<String>> mReceiveHeader;
    private List<ExtraHeader> mSendHeadExtra; //额外发送的头部信息
    private List<Pair<String,File>> mFiles;
    private List<Pair<String,String>> mParams;
    private RequestType mType = RequestType.DEFAULT;
    private Object mTag; //额外信息
    private Pair<String,String>[] mReceiveCookies; //留存的cookies
    private String mLastModified; //资源最后修改时间
    //加入activity或者fragment可以提升安全性
    private Activity mActivity;
    private Fragment mFragment;
    private Listener mListener;
    private Type[] mGenericType; //根据Listener生成的返回类类型存根
    private ServerCache mCacheManager; //缓存这个请求的数据管理
    private ThreadPoolExecutor mExecutor; //运行在指定线程池中,如果未指定默认在公共的线程池中
    private Request mNext;
    private MockProcess mMock; //模拟数据
    private ResponseStatus mResponseStatus=new ResponseStatus(); //返回包裹信息，尽量不要置null

    public static Request obtain() {
        return obtain("");
    }

    public static Request obtain(String url) {
        return obtain("POST", url);
    }

    public static Request obtain(String method, String url) {
        synchronized (sLock){
            if (sPool != null){
                Request r = sPool;
                sPool = r.mNext;
                r.mNext = null;
                sPoolSize--;
                r.setUrl(url);
                r.setMethod(method);
                return r;
            }
        }
        return new Request(method,url);
    }

    public Request() {
        this("");
    }

    public Request(String url) {
        this("POST", url);
    }

    /**
     * 使用模拟数据来初始化请求
     * @param mock
     */
    public Request(MockProcess mock){
        this("");
        mMock=mock;
    }

    public Request(String method, String url) {
        this.method = method.toUpperCase();
        mUrl = isReplaceChinese?transferSpaceAndChinese(url):url;
        isAcceptGlobalCallback=true;
        isReplaceChinese=true;
        isSendGzip=false;
        isReceiveGzip=false;
        useFactory = true;
        mParams = new ArrayList<>();
        mFiles = new ArrayList<>();
        mSendHeadExtra = new ArrayList<>();
    }

    /**
     * 清理这个请求以便重复使用
     */
    public void clear() {
        isReplaceChinese=true;
        useFactory = true;
        hadRootAddress = false;
        isSendGzip = false;
        isReceiveGzip = false;
        method = null;
        mUrl = null;
        mSendCookies = null;
        mDownloadable = null;
        if(mSendHeadExtra!=null)
            mSendHeadExtra.clear();
        if(mParams!=null)
            mParams.clear();
        if(mFiles!=null)
            mFiles.clear();
        mType = RequestType.DEFAULT;
        mTag = null;
        mReceiveCookies = null;
        mActivity = null;
        mFragment = null;
        mListener = null;
        mGenericType = null;
        mCacheManager = null;
        mExecutor = null;
        mMock=null;
        isAcceptGlobalCallback=true;
        mResponseStatus.clear();
        synchronized (sLock) {
            if (sPoolSize < MAX_POOL_SIZE) {
                mNext = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Request))
            return false;
        Request another = (Request) o;
        //如果url和上传的参数，文件都相同那么认为这个网络请求是同一个
        return another == this || (TextUtils.equals(mUrl,another.getUrl()) && another.getParamsRaw().equals(mParams) && another.getFiles().equals(mFiles));
    }

    public void start()  {
        start(false);
    }

    public void start(boolean forceRefresh, Fragment fragment)  {
        mFragment = fragment;
        start(forceRefresh);
    }

    public void start(boolean forceRefresh, Activity activity)  {
        mActivity = activity;
        start(forceRefresh);
    }

    public void start(boolean forceRefresh) {
        if (mCacheManager != null)
            mCacheManager.refresh(forceRefresh);
        else NetManager.getInstance().netRequest(this);
    }

    /**
     * 查找第一个某个键位置
     * @param key
     * @return
     */
    private int paramsIndexOf(String key){
        if(mParams!=null){
            for(int i=0;i<mParams.size();i++)
                if(TextUtils.equals(mParams.get(i).first,key))
                    return i;
        }
        return -1;
    }

    /**
     * 增加一组数据到某个键中
     * @param key
     * @param list
     * @param <T>
     * @return
     */
    public <T> Request addAll(String key, List<T> list){
        if(list!=null&&!list.isEmpty())
            for(T t:list)
                add(key,t);
        return this;
    }

    /**
     * 添加字符串请求参数
     * @param key
     * @param value
     * @return
     */
    public Request add(String key, String value){
        if(mParams==null)
            mParams=new ArrayList<>();
        mParams.add(Pair.create(key,value));
        return this;
    }

    /**
     * 添加整数
     * @param key
     * @param value
     * @return
     */
    public Request add(String key, int value){
        return  add(key,Integer.toString(value));
    }

    /**
     * 添加单精浮点数
     * @param key
     * @param value
     * @return
     */
    public Request add(String key, float value){
        return add(key,Float.toString(value));
    }

    /**
     * 添加长整形数
     * @param key
     * @param value
     * @return
     */
    public Request add(String key, long value){
        return add(key,Long.toString(value));
    }

    /**
     * 添加双精浮点数
     * @param key
     * @param value
     * @return
     */
    public Request add(String key, double value){
        return add(key,Double.toHexString(value));
    }

    /**
     * 添加短整型对象
     * @param key
     * @param value
     * @return
     */
    public Request add(String key, short value){
        return add(key,Short.toString(value));
    }

    /**
     * 添加Json对象
     * @param key
     * @param obj
     * @return
     */
    public Request add(String key, Object obj){
        return add(key,new Gson().toJson(obj));
    }

    /**
     * 添加字符串请求参数,如果存在则覆盖第一个
     * @param key
     * @param value
     */
    public Request put(String key, String value) {
        if (mParams == null)
            mParams = new ArrayList<>();
        int index=paramsIndexOf(key);
        if(index!=-1)
            mParams.remove(index);
        mParams.add(Pair.create(key,value));
        return this;
    }

    public Request put(String key, View view){
        if(view instanceof TextView)
            return put(key,((TextView)view).getText().toString());
        else if(view instanceof Spinner)
            return put(key,((Spinner)view).getSelectedItem());
        throw new IllegalArgumentException("不支持的view类型");
    }


    /**
     * 添加短整型请求参数,如果存在,覆盖第一个
     * @param key
     * @param value
     * @return
     */
    public Request put(String key, short value){
        return put(key,String.valueOf(value));
    }

    /**
     * 添加整型请求参数,如果存在,覆盖第一个
     * @param key
     * @param value
     * @return
     */
    public Request put(String key, int value) {
        return put(key, Integer.toString(value));
    }

    /**
     * 添加长整型请求参数,如果存在,覆盖第一个
     * @param key
     * @param value
     * @return
     */
    public Request put(String key, long value){
        return put(key,Long.toString(value));
    }

    /**
     * 添加单精浮点请求参数,如果存在,覆盖第一个
     * @param key
     * @param value
     * @return
     */
    public Request put(String key, float value){
        return put(key,String.valueOf(value));
    }

    /**
     * 添加双精浮点请求参数,如果存在,覆盖第一个
     * @param key
     * @param value
     * @return
     */
    public Request put(String key, double value){
        return put(key,String.valueOf(value));
    }

    /**
     * 添加json对象,如果存在,覆盖第一个
     * @param key
     * @param jsonObj
     * @return
     */
    public Request put(String key, Object jsonObj){
        return put(key,new Gson().toJson(jsonObj));
    }

    /**
     * 简易地添加请求参数
     * @param params
     * @return
     */
    public Request put(List<Pair<String,String>> params) {
        if (mParams == null)
            mParams = params;
        else
            mParams.addAll(params);
        return this;
    }


    /**
     * 发送文件
     * @param key
     * @param file
     * @return
     */
    public Request put(String key, File file) {
        if (mFiles == null)
            mFiles = new ArrayList<>();
        mFiles.add(new Pair<>(key,file));
        return this;
    }

    /**
     * 发送文件列表
     * @param fileParams
     * @return
     */
    public Request putFile(List<Pair<String,File>> fileParams) {
        if (mFiles == null) mFiles = fileParams;
        else mFiles.addAll(fileParams);
        return this;
    }

    /**
     * 参数递增
     * @param key
     * @param count
     */
    public void increment(String key, int count){
        int index;
        if ((index=checkNumberParams(key))==-1)
            return;
        Pair<String,String> pair=mParams.get(index);
        int value=Integer.parseInt(pair.second);
        mParams.remove(pair);
        mParams.add(0, Pair.create(key,Integer.toString(value+count)));
    }

    /**
     * 参数递减
     * @param key
     * @param count
     */
    public void decrease(String key, int count){
        int index;
        if ((index=checkNumberParams(key))==-1)
            return;
        Pair<String,String> pair=mParams.get(index);
        int value=Integer.parseInt(pair.second);
        mParams.remove(pair);
        mParams.add(0, Pair.create(key,Integer.toString(value-count)));
    }

    /**
     * 检查是否是数字参数
     * @param key
     * @return
     */
    private int checkNumberParams(String key){
        int index=-1;
        if (mParams == null) {
            mParams = new ArrayList<>();
            mParams.add(Pair.create(key,"0"));
            return index;
        }
        if ((index=paramsIndexOf(key))==-1) {
            mParams.add(Pair.create(key,"0"));
            return index;
        }
        try {
            Integer.parseInt(mParams.get(index).second);
            return index;
        } catch (NumberFormatException e){
            //转换异常不处理
        }
        return index;
    }

    /**
     * 获取类型索引
     *
     * @param sb
     * @return
     */
    private int getTypeIndex(StringBuilder sb) {
        int index = sb.indexOf(",");
        if (index == -1)
            return -1;
        String strIndex = sb.substring(index + 1);
        try {
            sb.delete(sb.length() - 2, sb.length());
            return Integer.parseInt(strIndex);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public Map<String,String> getParams(){
        Map<String,String> map=new HashMap<>();
        if(mParams==null) return map;
        for(Pair<String,String> pair:mParams)
            map.put(pair.first,pair.second);
        return map;
    }

    public List<Pair<String,String>> getParamsRaw(){
        return mParams;
    }

    /**
     * 发送参数
     * @param params
     */
    public void setParams(List<Pair<String,String>> params) {
        if (params == null&&mParams!=null)
            mParams.clear();
        else
            mParams = params;
    }

    /**
     * 发送文件列表
     * @param files 字符串键文件值对
     */
    public void setFiles(List<Pair<String,File>> files) {
        if (files == null) mFiles.clear();
        else mFiles = files;
    }

    public List<Pair<String,File>> getFiles() {
        return mFiles;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    /**
     * 设置网络回调监听
     * @param l 监听器
     * @return 网络请求本身
     */
    public Request setListener(final Listener l){
        mListener=l;
        if(mGenericType!=null&&mGenericType.length>0) //如果指定了实体类型，不自动解析实体类型
            return this;
        mGenericType=new Type[3];
        //泛型解析,如果是Object和byte[]就返回原始字节流,String返回字符,其它类型就尝试使用gson解析
        Method[] ms = l.getClass().getDeclaredMethods();
        List<Method> duplicate = new ArrayList<>();
        for (Method m : ms) {
            if ("onResponseListener".equals(m.getName()))
                duplicate.add(m);
        }
        for (Method m : duplicate){
            boolean someoneIsNotObject=false;
            Type[] types=m.getGenericParameterTypes();
            if(types!=null){
                if (types.length>1&&types[1] != Object.class){
                    mGenericType[0] = types[1];
                    someoneIsNotObject=true;
                }
                if(types.length>2&&types[2]!=Object.class){
                    mGenericType[1]=types[2];
                    someoneIsNotObject=true;
                }
                if(types.length>3&&types[3]!=Object.class){
                    mGenericType[2]=types[3];
                    someoneIsNotObject=true;
                }
            }
            if(someoneIsNotObject) break;
        }
        return this;
    }

    /**
     * 取消网络请求
     */
    public void cancel(){
        if(mListener != null)
            mListener.onErrorListener(this, "取消请求 " + mUrl);
    }

    /**
     * 开启缓存时间,这个方法需要在设置回调后使用
     * @param context   上下文
     * @param cacheName 缓存名（唯一缓存名）
     */
    public Request setCacheTime(Context context, String cacheName, long liveTime) {
        setCacheTime(context, cacheName, liveTime, null);
        return this;
    }

    /**
     * 开启缓存时间,这个方法需要在设置回调后使用,如果这个请求有指定运行线程池则指定缓存器也使用这个线程池
     * @param context    上下文
     * @param cacheName  缓存名（唯一缓存名）
     * @param toDatabase 保持到指定数据库
     */
    public Request setCacheTime(Context context, String cacheName, long liveTime, @Nullable String toDatabase) {
        FastDatabase database = TextUtils.isEmpty(toDatabase) ? FastDatabase.getDefaultInstance(context) : FastDatabase.getInstance(context,toDatabase);
        if (mExecutor == null)
            mCacheManager = new ServerCache(this, cacheName, database);
        else
            mCacheManager = new ServerCache(this, cacheName, database, mExecutor);
        mCacheManager.setCacheTimeLife(liveTime);
        return this;
    }

    /**
     * 空格和汉字替换成unicode
     * @param str
     * @return
     */
    private String transferSpaceAndChinese(String str){
        if(TextUtils.isEmpty(str))
            return "";
        StringBuilder sb=new StringBuilder(str);

        for(int i=0;i<sb.length();i++){
            char c=sb.charAt(i);
            if(c>='\u4e00'&&c<='\u9fa5'){
                try {
                    sb.deleteCharAt(i);
                    sb.insert(i,URLEncoder.encode(String.valueOf(c),"UTF-8").toCharArray());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString().replace(" ","%20"); //最后空格置换
    }

    public byte[] getByteStream() {
        return mByteStream;
    }

    public Request setByteStream(byte[] byteStream) {
        mByteStream = byteStream;
        return this;
    }

    public Listener getListener() {
        return mListener;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getMethod() {
        return method;
    }

    public Request setMethod(String method) {
        this.method = method.toUpperCase();
        return this;
    }

    public Request setDownloadable(Downloadable d) {
        mDownloadable = d;
        return this;
    }

    public Downloadable getDownloadable() {
        return mDownloadable;
    }

    public boolean isUseFactory() {
        return useFactory;
    }

    public Request setUseFactory(boolean useFactory) {
        this.useFactory = useFactory;
        return this;
    }

    public boolean isHadRootAddress() {
        return hadRootAddress;
    }

    public void setHadRootAddress(boolean hadRootAddress) {
        this.hadRootAddress = hadRootAddress;
    }

    public boolean downloadable() {
        return mDownloadable != null && mDownloadable.getTargetFile() != null && mDownloadable.getTargetFile().exists();
    }

    public Request setHost(Context context){
        if (context instanceof Activity)
            mActivity = (Activity) context;
        return this;
    }

    public Request setHost(Activity activity) {
        mActivity = activity;
        return this;
    }

    public Request setHost(Fragment fragment) {
        mFragment = fragment;
        return this;
    }

    public Pair<String,String>[] getReceiveCookies() {
        return mReceiveCookies;
    }

    public void setReceiveCookies(Pair<String,String>[] receiveCookies) {
        mReceiveCookies = receiveCookies;
    }

    public Object getTag() {
        return mTag;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public RequestType getType() {
        return mType;
    }

    public void setType(RequestType type) {
        mType = type;
    }

    public List<Pair<String, String>> getSendCookies() {
        return mSendCookies;
    }

    public void setSendCookies(List<Pair<String, String>> sendCookies) {
        mSendCookies = sendCookies;
    }

    public void putSendCookies(String key,String value){
        if(mSendCookies==null)
            mSendCookies=new ArrayList<>();
        mSendCookies.add(Pair.create(key,value));
    }

    public void setGenericType(Type[] type) {
        mGenericType = type;
    }

    public Type[] getGenericType() {
        return mGenericType;
    }

    public boolean isReceiveGzip() {
        return isReceiveGzip;
    }

    public void setReceiveGzip(boolean receiveGzip) {
        isReceiveGzip = receiveGzip;
    }

    public boolean isSendGzip() {
        return isSendGzip;
    }

    public void setSendGzip(boolean sendGzip) {
        isSendGzip = sendGzip;
    }

    public Request putHeader(String key, String value){
        if(mSendHeadExtra ==null)
            mSendHeadExtra =new ArrayList<>();
        ExtraHeader extra=new ExtraHeader();
        extra.canDuplication =false;
        extra.field=key;
        extra.value=value;
        mSendHeadExtra.add(extra);
        return this;
    }

    public Request removeHeader(String key){
        if(mSendHeadExtra ==null) return this;
        List<ExtraHeader> needDeleteList=new ArrayList<>();
        for(ExtraHeader head: mSendHeadExtra)
            if(head.field.equals(key))
                needDeleteList.add(head);
        for(ExtraHeader head:needDeleteList)
            mSendHeadExtra.remove(head);
        return this;
    }

    public Request addHeader(String key, String value){
        if(mSendHeadExtra ==null)
            mSendHeadExtra =new ArrayList<>();
        ExtraHeader extra=new ExtraHeader();
        extra.canDuplication =true;
        extra.field=key;
        extra.value=value;
        mSendHeadExtra.add(extra);
        return this;
    }

    public void setSendHeader(List<ExtraHeader> headers){
        mSendHeadExtra=headers;
    }

    public List<ExtraHeader> getSendHeadExtra() {
        return mSendHeadExtra;
    }

    public ServerCache getCacheManager() {
        return mCacheManager;
    }

    public ThreadPoolExecutor getExecutor() {
        return mExecutor;
    }

    public Request setExecutor(ThreadPoolExecutor executor) {
        mExecutor = executor;
        return this;
    }

    public MockProcess getMock() {
        return mMock;
    }

    public Request setMock(MockProcess mock) {
        mMock = mock;
        return this;
    }

    public String getLastModified(){
        return mLastModified;
    }

    public void setLastModified(String lastModified) {
        mLastModified = lastModified;
    }

    public Object getHost() {
        if (mFragment != null)
            return mFragment;
        if (mActivity != null)
            return mActivity;
        return null;
    }

    public ResponseStatus getResponseStatus() {
        return mResponseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus){
        if(responseStatus==null) mResponseStatus.clear();
        else mResponseStatus = responseStatus;
    }

    public Map<String, List<String>> getReceiveHeader() {
        return mReceiveHeader;
    }

    public void setReceiveHeader(Map<String, List<String>> receiveHeader) {
        mReceiveHeader = receiveHeader;
    }

    public boolean isReplaceChinese() {
        return isReplaceChinese;
    }

    public void setReplaceChinese(boolean replaceChinese) {
        isReplaceChinese = replaceChinese;
    }

    public boolean isAcceptGlobalCallback() {
        return isAcceptGlobalCallback;
    }

    public void setAcceptGlobalCallback(boolean acceptGlobalCallback) {
        isAcceptGlobalCallback = acceptGlobalCallback;
    }

    @Override
    public String toString(){
        StringBuilder paramsStr=new StringBuilder();
        StringBuilder uploadFileStr=new StringBuilder();

        paramsStr.append("params:").append("[");
        if(mParams!=null&&!mParams.isEmpty()){
            for(Pair<String,String> pair:mParams)
                paramsStr.append("{").append(pair.first).append(",").append(pair.second).append("}").append(",");
            paramsStr.deleteCharAt(paramsStr.length()-1); //去掉最后的逗号
        }
        paramsStr.append("]");
        uploadFileStr.append("files:").append("[");
        if(mFiles!=null&&!mFiles.isEmpty()){
            for(Pair<String,File> pair:mFiles)
                uploadFileStr.append("{").append(pair.first).append(",").append(pair.second).append("}").append(",");
            uploadFileStr.deleteCharAt(uploadFileStr.length()-1);
        }
        uploadFileStr.append("]");
        return "URL:" + mUrl + " Method:" + method + "\n" +
                paramsStr.toString() + "\n" +
                uploadFileStr.toString();
    }

    /**
     * 请求类型</br>
     * 1.默认 一切行动照正常规则来</br>
     * 2.全局请求 不受模块限制，独立于模块之外</br>
     * 3.必达请求 在请求开始的时候存入数据库，仅成功送达后删除，并且在错误返回后重试
     */
    public enum RequestType {
        DEFAULT,
        GLOBAL,
        MUSTSEND
    }

    public static class ExtraHeader {
        public boolean canDuplication; //add或者put
        public String field;
        public String value;

        public ExtraHeader(){}

        public ExtraHeader(boolean canDuplication, String field, String value) {
            this.canDuplication = canDuplication;
            this.field = field;
            this.value = value;
        }
    }
}