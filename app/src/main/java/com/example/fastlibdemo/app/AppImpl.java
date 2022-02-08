package com.example.fastlibdemo.app;

import android.app.Application;
import android.os.Build;
import android.os.Process;
import android.webkit.WebView;

import com.fastlib.db.FastDatabase;
import com.fastlib.net.HttpGlobalConfig;
import com.fastlib.net.Request;
import com.fastlib.net.listener.GlobalListener;
import com.fastlib.utils.AppUtil;
import com.fastlib.utils.FastLog;


/**
 * Created by liuwp on 2020/3/28.
 */
public class AppImpl extends Application{
    public static Application instance;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
        initFastLib();
        initWebView();
    }

    private void initFastLib(){
        FastLog.setDebug(true,false,0,"FASTER");
        FastDatabase.getConfig().setVersion(12);
        HttpGlobalConfig.getInstance().setRootAddress("https://www.xxx.com:443");
        HttpGlobalConfig.getInstance().setGlobalListener(new GlobalListener(){
            @Override
            public void onLaunchRequestBefore(Request request){
                super.onLaunchRequestBefore(request);
            }

            @Override
            public byte[] onRawDataListener(Request request,byte[] data){
                request.printRequestAndResponse(data);
                return super.onRawDataListener(request,data);
            }

            @Override
            public Object onResponseSuccessListener(Request request,Object result){
                return super.onResponseSuccessListener(request,result);
            }

            @Override
            public Exception onErrorListener(Request request,Exception error){
                FastLog.e(error.toString());
                return super.onErrorListener(request,error);
            }
        });
    }

    private void initWebView(){
        //Android P行为变更，WebView不可多进程使用同一个目录，需要为不同进程设置不同目录.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            String currentProcessName = AppUtil.getProcessName(this,Process.myPid());
            String packageName = AppUtil.getPackageName(this);
            //FastLog.d("currentProcessName:"+currentProcessName+"   packageName:"+packageName);
            if(currentProcessName != null && !currentProcessName.equalsIgnoreCase(packageName)){
                WebView.setDataDirectorySuffix(currentProcessName);
            }
        }
    }
}
