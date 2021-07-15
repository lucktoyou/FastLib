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
public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FastDatabase.getConfig().setVersion(6);
        HttpGlobalConfig.getInstance().setRootAddress("https://www.xxx.com:443");
        HttpGlobalConfig.getInstance().setGlobalListener(new GlobalListener(){

            @Override
            public void onLaunchRequestBefore(Request request) {
                super.onLaunchRequestBefore(request);
            }

            @Override
            public byte[] onRawData(Request request, byte[] data) {
                request.printRequestAndResponse(data);
                return super.onRawData(request,data);
            }

            @Override
            public Object onResponseListener(Request request, Object result) {
                return super.onResponseListener(request, result);
            }

            @Override
            public Exception onErrorListener(Request request, Exception error) {
                FastLog.e(error.toString());
                return super.onErrorListener(request, error);
            }

            @Override
            public void onRequestComplete() {
                super.onRequestComplete();
            }
        });

        //Android P行为变更，WebView不可多进程使用同一个目录，需要为不同进程设置不同目录.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String currentProcessName = AppUtil.getProcessName(this, Process.myPid());
            String packageName = AppUtil.getPackageName(this);
            //FastLog.d("currentProcessName:"+currentProcessName+"   packageName:"+packageName);
            if(currentProcessName !=null && !currentProcessName.equalsIgnoreCase(packageName)){
                WebView.setDataDirectorySuffix(currentProcessName);
            }
        }
    }
}
