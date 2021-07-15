package com.fastlib.utils.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Author: TinhoXu
 * E-mail: xth@erongdu.com
 * Date: 2016/11/11 16:46
 * Description: application_context 工具类
 */
@SuppressWarnings("all")
public class ContextHolder {
    private static Context APPLICATION_CONTEXT;

    /**
     * 初始化context，如果由于不同机型导致反射获取context失败可以在Application调用此方法
     */
    public static void init(Context context) {
        APPLICATION_CONTEXT = context.getApplicationContext();
    }

    public static Context getContext() {
        return getContext(null);
    }

    /**
     * 反射获取 application context
     */
    public static Context getContext(@Nullable Object host) {
        if (host != null) {
            if (host instanceof Activity) {
                APPLICATION_CONTEXT = ((Activity) host).getApplication();
            } else if (host instanceof Fragment) {
                APPLICATION_CONTEXT = ((Fragment) host).getContext().getApplicationContext();
            }
        }
        if (null == APPLICATION_CONTEXT) {
            try {
                Application application = (Application) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication")
                        .invoke(null, (Object[]) null);
                if (application != null) {
                    APPLICATION_CONTEXT = application;
                    return application;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Application application = (Application) Class.forName("android.app.AppGlobals")
                        .getMethod("getInitialApplication")
                        .invoke(null, (Object[]) null);
                if (application != null) {
                    APPLICATION_CONTEXT = application;
                    return application;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return APPLICATION_CONTEXT;
    }
}