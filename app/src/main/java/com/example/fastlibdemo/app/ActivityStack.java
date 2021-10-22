package com.example.fastlibdemo.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.fastlib.utils.FastLog;

import java.util.Vector;

public class ActivityStack{
    private Vector<Activity> mStack;

    public ActivityStack(Application application){
        mStack=new Vector<>();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity,Bundle savedInstanceState){
                mStack.add(activity);
                StringBuilder sb=new StringBuilder();
                for(Activity active:mStack)
                    sb.append("->").append(active.getClass().getSimpleName());
                FastLog.d("add "+activity.getClass().getSimpleName()+" \n"+sb);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity,Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                mStack.remove(activity);
                StringBuilder sb=new StringBuilder();
                for(Activity active:mStack)
                    sb.append("->").append(active.getClass().getSimpleName());
                FastLog.d("remove "+activity.getClass().getSimpleName()+"\n"+sb);
            }
        });
    }

    public void clearStack(){
        for(Activity activity:mStack)
            activity.finish();
        mStack.clear();
    }
}