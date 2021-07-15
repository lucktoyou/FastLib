package com.fastlib.utils;

import android.text.TextUtils;

import com.fastlib.BuildConfig;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

/**
 * Created by liuwp on 2020/8/12.
 * 日志打印工具,说明：
 * 1，目的：规范日志输出。
 * 2，优势：①可以控制是否输出日志.②可在打印的日志中点击跳转到日志输出位置.③可以灵活更换日志库.
 * 3.使用范围：打印库内、库外日志信息。
 */
public class FastLog{
    private static final boolean debug = BuildConfig.DEBUG;
    private static final PrettyFormatStrategy strategy = PrettyFormatStrategy.newBuilder().showThreadInfo(true).methodCount(2).tag("PRETTY_LOG").build();
    private static final AndroidLogAdapter adapter = new AndroidLogAdapter(strategy);

    public static void d(String msg){
        if(debug && !TextUtils.isEmpty(msg)){
            Logger.clearLogAdapters();
            Logger.addLogAdapter(adapter);
            Logger.d(msg);
        }
    }

    public static void i(String msg){
        if(debug && !TextUtils.isEmpty(msg)){
            Logger.clearLogAdapters();
            Logger.addLogAdapter(adapter);
            Logger.i(msg);
        }
    }

    public static void w(String msg){
        if(debug && !TextUtils.isEmpty(msg)){
            Logger.clearLogAdapters();
            Logger.addLogAdapter(adapter);
            Logger.w(msg);
        }
    }

    public static void e(String msg){
        if(debug && !TextUtils.isEmpty(msg)){
            Logger.clearLogAdapters();
            Logger.addLogAdapter(adapter);
            Logger.e(msg);
        }
    }
}
