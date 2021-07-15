package com.fastlib.net;

import androidx.annotation.NonNull;

import com.fastlib.net.listener.GlobalListener;

/**
 * Created by sgfb on 2019\12\23.
 */
public class HttpGlobalConfig {
    private static HttpGlobalConfig mInstance;
    private GlobalListener mGlobalListener;
    private GlobalListener mEmptyGlobalListener=new GlobalListener();
    private String mRootAddress;

    public static HttpGlobalConfig getInstance(){
        if(mInstance==null){
            synchronized (HttpGlobalConfig.class){
                mInstance=new HttpGlobalConfig();
            }
        }
        return mInstance;
    }

    private HttpGlobalConfig(){

    }

    public void setRootAddress(String rootAddress){
        mRootAddress=rootAddress;
    }

    public @NonNull
    String getRootAddress(){
        if(mRootAddress==null) return "";
        return mRootAddress;
    }

    public void setGlobalListener(GlobalListener globalListener){
        mGlobalListener=globalListener;
    }

    public @NonNull
    GlobalListener getGlobalListener(){
        if(mGlobalListener==null) return mEmptyGlobalListener;
        return mGlobalListener;
    }

    public @NonNull
    GlobalListener getEmptyGlobalListener(){
        return mEmptyGlobalListener;
    }
}
