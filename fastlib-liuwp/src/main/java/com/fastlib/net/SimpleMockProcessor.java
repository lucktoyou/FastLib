package com.fastlib.net;

import android.content.res.AssetManager;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.fastlib.db.SaveUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by sgfb on 17/2/6.
 * 一个简单的模拟数据返回
 */
public class SimpleMockProcessor implements MockProcess {
    private String mJsonData;
    private byte[] mVerifyFailureResult;
    private File mDataFile;
    private AssetManager mAssetManager;
    private Pair<String,String>[] mVerify;

    public SimpleMockProcessor(String json){
        mJsonData=json;
    }

    public SimpleMockProcessor(File file){
        this(file,null);
    }

    public SimpleMockProcessor(File path, AssetManager am){
        mDataFile=path;
        mAssetManager=am;
    }

    /**
     * 验证参数。如果Request中有且参数一致的情况下返回正常数据，否则返回制定数据
     * @param data 验证失败时返回的数据
     * @param params 验证参数
     * @return 数据模拟器
     */
    public SimpleMockProcessor verify(byte[] data, Pair<String,String>[] params){
        mVerify=params;
        mVerifyFailureResult=data;
        return this;
    }

    /**
     * 验证参数。如果Request中有且参数一致的情况下返回正常数据，否则返回制定数据
     * @param assetManager Assets文件管理器
     * @param file 文件路径
     * @param params 验证参数
     * @return 数据模拟期
     * @throws IOException
     */
    public SimpleMockProcessor verify(AssetManager assetManager, File file, Pair<String,String>[] params) throws IOException {
        if(assetManager!=null) return verify(SaveUtil.loadAssetsFile(assetManager,file.getPath()),params);
        else return verify(SaveUtil.loadFile(file.getAbsolutePath()),params);
    }

    @Override
    public byte[] dataResponse(Request request){
        try{
            //验证参数
            if(mVerify!=null&&mVerify.length>0){
                for(Pair<String,String> pair:mVerify){
                    Map<String,String> requestParams=request.getParams();
                    if(!requestParams.containsKey(pair.first)||!requestParams.get(pair.first).equals(pair.second))
                        return mVerifyFailureResult;
                }
            }
            if(!TextUtils.isEmpty(mJsonData)) return mJsonData.getBytes();
            else if(mAssetManager!=null) return SaveUtil.loadAssetsFile(mAssetManager,mDataFile.getPath());
            else SaveUtil.loadFile(mDataFile.getAbsolutePath());
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}