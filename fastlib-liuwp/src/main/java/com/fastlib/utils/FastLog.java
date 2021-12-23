package com.fastlib.utils;

import androidx.annotation.Nullable;

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

    static{
        setDebug(BuildConfig.DEBUG);
    }

    public static void setDebug(boolean debug){
        setDebug(debug,false,0,"FASTER");
    }

    public static void setDebug(final boolean debug,boolean showThreadInfo,int methodCount,String tag){
        PrettyFormatStrategy strategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(showThreadInfo).methodCount(methodCount).tag(tag)
                .build();
        AndroidLogAdapter adapter = new AndroidLogAdapter(strategy){
            @Override
            public boolean isLoggable(int priority,@Nullable String tag){
                return debug;
            }
        };
        Logger.clearLogAdapters();
        Logger.addLogAdapter(adapter);
    }

    public static void d(String msg){
        Logger.d(msg);
    }

    public static void i(String msg){
        Logger.i(msg);
    }

    public static void w(String msg){
        Logger.w(msg);
    }

    public static void e(String msg){
        Logger.e(msg);
    }
}
