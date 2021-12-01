package com.fastlib.net;

import androidx.annotation.NonNull;

import com.fastlib.net.listener.GlobalListener;
import com.fastlib.net.listener.SimpleListener;

import java.lang.reflect.Type;

/**
 * Created by sgfb on 2019\12\23.
 */
public class HttpGlobalConfig{
    private static HttpGlobalConfig mInstance;
    private String mRootAddress;
    private GlobalListener mGlobalListener;
    private GlobalListener mEmptyGlobalListener = new GlobalListener();
    private SimpleListener mEmptyListener = new SimpleListener(){
        @Override
        public byte[] onRawData(Request request,byte[] data,Type type){
            return super.onRawData(request,data,type);
        }

        @Override
        public void onResponseSuccess(Request request,Object result){

        }

        @Override
        public void onError(Request request,Exception error){

        }
    };

    public static HttpGlobalConfig getInstance(){
        if(mInstance == null){
            synchronized(HttpGlobalConfig.class){
                mInstance = new HttpGlobalConfig();
            }
        }
        return mInstance;
    }

    private HttpGlobalConfig(){

    }

    public void setRootAddress(String rootAddress){
        mRootAddress = rootAddress;
    }

    public @NonNull
    String getRootAddress(){
        if(mRootAddress == null) return "";
        return mRootAddress;
    }

    public void setGlobalListener(GlobalListener globalListener){
        mGlobalListener = globalListener;
    }

    public @NonNull
    GlobalListener getGlobalListener(){
        if(mGlobalListener == null) return mEmptyGlobalListener;
        return mGlobalListener;
    }

    public @NonNull
    GlobalListener getEmptyGlobalListener(){
        return mEmptyGlobalListener;
    }

    public @NonNull
    SimpleListener getEmptyListener(){
        return mEmptyListener;
    }
}
