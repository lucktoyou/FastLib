package com.fastlib.db;

import com.fastlib.bean.RemoteCache;
import com.fastlib.net.Listener;
import com.fastlib.net.NetManager;
import com.fastlib.net.Request;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 16/12/29.
 * 缓存来自服务器的数据
 */
public class ServerCache {
    private long mCacheTimeLife =0; //缓存生存长度
    private RemoteCache mCache;
    private ThreadPoolExecutor mThreadPool;
    private Request mRequest;
    private FastDatabase mDatabase;
    private Listener mOldListener;
    private String mCacheName;

    /**
     * 缓存管理构造
     * @param request 网络请求
     * @param cacheName 缓存名
     * @param database 指定的数据库
     */
    public ServerCache(Request request, String cacheName, FastDatabase database){
        this(request,cacheName,database,null);
    }

    /**
     * 指定某线程池的缓存管理
     * @param request 网络请求
     * @param cacheName 缓存名
     * @param database 指定的数据库
     * @param threadPool 线程池
     */
    public ServerCache(Request request, String cacheName, FastDatabase database, ThreadPoolExecutor threadPool){
        mThreadPool=threadPool;
        mRequest=request;
        mDatabase =database;
        mCacheName=cacheName;
        init();
    }

    private void init(){
        mCache=mDatabase.setFilter(And.condition(Condition.equal(mCacheName))).getFirst(RemoteCache.class); //尝试从数据库中获取一下缓存
        if(mCache==null) {
            mCache = new RemoteCache();
            mCache.cacheName=mCacheName;
        }
        mOldListener=mRequest.getListener();
        mRequest.setListener(new Listener(){

            @Override
            public void onRawData(Request r, final byte[] data) {
                if(mOldListener!=null)
                    mOldListener.onRawData(r,data);
                if(mThreadPool!=null)
                    mThreadPool.execute(new Runnable(){
                        @Override
                        public void run(){
                            saveCache(new String(data));
                        }
                    });
                else
                    saveCache(new String(data));
            }

            @Override
            public void onTranslateJson(final Request r, final String json) {
                if(mOldListener!=null)
                    mOldListener.onTranslateJson(r,json);
            }

            @Override
            public void onResponseListener(Request r, Object result,Object result2,Object result3){
                if(mOldListener!=null)
                    mOldListener.onResponseListener(r,result,result2,result3);
            }

            @Override
            public void onErrorListener(Request r, String error) {
                if(mOldListener!=null)
                    mOldListener.onErrorListener(r,error);
            }
        });
    }

    /**
     * 重新标上缓存时间,保存缓存
     * @param json
     */
    private void saveCache(String json){
        mCache.expiry=System.currentTimeMillis()+mCacheTimeLife;
        mCache.cache=json;
        mDatabase.saveOrUpdate(mCache);
    }

    /**
     * 刷新缓存,如果不强制刷新则根据生存时间来决定
     * @param force 是否强制刷新
     */
    public void refresh(boolean force){
        if(force||mCache.expiry<System.currentTimeMillis())
            NetManager.getInstance().netRequest(mRequest);
        else
            toggleCallback();
    }

    /**
     * 使用缓存触发数据回调
     */
    private void toggleCallback(){
        Gson gson=new Gson();
        mOldListener.onTranslateJson(mRequest,mCache.cache);
        Type[] type=mRequest.getGenericType();
        Type realType=entityType(type);
        if(isStrType(type))
            mOldListener.onResponseListener(mRequest,mCache.cache,null,null);
        else if(realType!=null)
            mOldListener.onResponseListener(mRequest,gson.fromJson(mCache.cache,realType),null,null);
    }

    private Type entityType(Type[] types){
        if(types==null||types.length==0) return null;
        for(Type type:types)
            if(type!=Object.class&&type!=byte[].class&&type!=String.class)
                return type;
        return null;
    }

    private boolean isStrType(Type[] types){
        if(types==null||types.length==0) return false;
        boolean hadStrType=false;
        boolean hadOtherType=false;
        for(Type type:types)
            if(type==String.class)
                hadStrType=true;
            else if(type!=Object.class&&type!=byte[].class)
                hadOtherType=true;
        return hadStrType&&!hadOtherType;
    }

    public long getCacheTimeLife() {
        return mCacheTimeLife;
    }

    public void setCacheTimeLife(long cacheTimeLife) {
        mCacheTimeLife = cacheTimeLife;
    }

    public Request getRequest() {
        return mRequest;
    }

    public void setRequest(Request request) {
        mRequest = request;
    }

    public void setThreadPool(ThreadPoolExecutor threadPool) {
        mThreadPool = threadPool;
    }

    public void setDatabase(FastDatabase database) {
        mDatabase = database;
    }
}
