package com.fastlib.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fastlib.BuildConfig;
import com.fastlib.annotation.Event;
import com.fastlib.net.NetManager;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 16/9/1.
 */
public class EventObserver {
    private static final String TAG=EventObserver.class.getSimpleName();

    private static EventObserver mOwer;
    private Map<String,LocalReceiver> mNameToObserver;   //订阅事件名->订阅广播
    private Map<Object,List<String>> mSubscriberToEvent; //订阅者->订阅事件名

    private EventObserver(){
        mNameToObserver =new HashMap<>();
        mSubscriberToEvent =new HashMap<>();
    }

    public static synchronized EventObserver getInstance(){
        if(mOwer==null)
            mOwer=new EventObserver();
        return mOwer;
    }

    /**
     * 订阅注册者所有有Event注解的本地事件.这个方法将会遍历订阅者所有方法
     * @param subscriber 订阅者
     */
    public synchronized void subscribe(Context context,Object subscriber){
        List<String> eventNames= mSubscriberToEvent.get(subscriber);
        List<Method> eventMethods=findEventMethods(subscriber);

        if(eventMethods==null||eventMethods.isEmpty()){
            Log.d(TAG,"订阅者"+subscriber+"没有广播接收方法,请检查是否添加了Event注解和广播方法参数");
            return;
        }
        if(eventNames==null) {
            eventNames = new ArrayList<>();
            mSubscriberToEvent.put(subscriber,eventNames);
        }
        for(Method m:eventMethods){
            String eventName=baseClassUpper(m.getParameterTypes()[0]); //仅取第一个参数作为广播目标
            LocalReceiver receiver= mNameToObserver.get(eventName);
            IntentFilter filter=new IntentFilter(eventName);
            if(receiver==null){
                receiver = new LocalReceiver();
                mNameToObserver.put(eventName,receiver);
                LocalBroadcastManager.getInstance(context).registerReceiver(receiver,filter);
            }
            if(!eventNames.contains(eventName)) {
                eventNames.add(eventName);
                receiver.mSubscribes.put(subscriber,m);
            }
            Log.d(TAG,"订阅者"+subscriber+"订阅事件"+eventName);
        }
    }

    public void unsubscribe(Context context,Object subscriber,Class<?> event){
        unsubscribe(context,subscriber,baseClassUpper(event));
    }

    /**
     * 移除某订阅者所有订阅事件
     * @param subscribe 订阅者
     */
    public synchronized void unsubscribe(Context context,Object subscribe){
        List<String> events= mSubscriberToEvent.get(subscribe);
        if(events==null||events.size()<=0)
            return;
        int count=events.size();
        for(int i=0;i<count;i++) {
            String event=events.get(0);
            unsubscribe(context, subscribe, event);
        }
    }

    /**
     * 移除某订阅者的某个订阅事件
     * @param subscriber 订阅者
     * @param eventName 订阅事件名
     */
    public void unsubscribe(Context context,Object subscriber,String eventName){
        List<String> events= mSubscriberToEvent.get(subscriber);
        LocalReceiver receiver= mNameToObserver.get(eventName);

        if(events==null) //如果这个订阅者还没订阅过事件就跳出
            return;
        events.remove(eventName);
        receiver.mSubscribes.remove(subscriber);
        if(receiver.mSubscribes.size()==0){
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            mNameToObserver.remove(eventName);
        }
        if(events.size()==0)
            mSubscriberToEvent.remove(subscriber);
        Log.d(TAG, "订阅者" + subscriber + "移除事件"+eventName);
    }

    /**
     * 发送本地事件
     * @param event
     */
    public void sendEvent(Context context,Object event){
        if(event==null){
            Log.d(TAG,"无法发送null事件");
            return;
        }
        String name=event.getClass().getCanonicalName();
        Intent intent=new Intent(name);
        EntityWrapper entity=new EntityWrapper(event);
        intent.putExtra("entity",entity);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Log.d(TAG,"广播事件"+name);
    }

    /**
     * 应用退出时应该调用这个方法清理所有数据
     */
    public void close(Context context){
        if (BuildConfig.DEBUG)
            Log.d(TAG,"清理所有数据");
        clearReceiver(context);
        mNameToObserver.clear();
        mSubscriberToEvent.clear();
        mOwer=null;

        System.gc();
    }

    /**
     * 清理所有广播
     */
    private void clearReceiver(Context context){
        Iterator<String> iter= mNameToObserver.keySet().iterator();
        while(iter.hasNext()){
            String key=iter.next();
            LocalReceiver receiver= mNameToObserver.get(key);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        }
    }

    /**
     * 返回类型名，如果是基本类型转换为引用类
     * @param cla
     * @return
     */
    private String baseClassUpper(Class<?> cla){
        String name=cla.getCanonicalName();
        if(name.equals(int.class.getCanonicalName()))
            return Integer.class.getCanonicalName();
        else if(name.equals(long.class.getCanonicalName()))
            return Long.class.getCanonicalName();
        else if(name.equals(float.class.getCanonicalName()))
            return Float.class.getCanonicalName();
        else if(name.equals(double.class.getCanonicalName()))
            return Double.class.getCanonicalName();
        else if(name.equals(short.class.getCanonicalName()))
            return Short.class.getCanonicalName();
        return name;
    }

    /**
     * 遍历注解Event的接受广播方法
     * @param subscriber 订阅者
     * @return 具有Event注解的方法数组
     */
    private List<Method> findEventMethods(Object subscriber){
        List<Method> eventMethods=new ArrayList<>();
        Method[] allMethod=subscriber.getClass().getDeclaredMethods();
        if(allMethod==null||allMethod.length==0)
            return null;
        for(Method m:allMethod){
            Annotation eventAnnotation=m.getAnnotation(Event.class);
            if(eventAnnotation!=null){
                Class<?>[] params=m.getParameterTypes();
                if(params!=null&&params.length>0) //判断广播接收事件参数是否正常
                    eventMethods.add(m);
            }
        }
        return eventMethods;
    }

    /**
     * 事件广播中转.运行在主线程中
     */
    public class LocalReceiver extends BroadcastReceiver{
        public Map<Object,Method> mSubscribes; //订阅者->事件方法

        public LocalReceiver(){
            mSubscribes=new HashMap<>();
        }

        @Override
        public void onReceive(Context context, Intent intent){
            final EntityWrapper wrapper= (EntityWrapper) intent.getSerializableExtra("entity");
            List<Object> invisibleSubscriber=new ArrayList<>();
            Iterator<Object> iter=mSubscribes.keySet().iterator();
            while(iter.hasNext()){
                final Object subscribe=iter.next();
                boolean visible=checkVisible(subscribe);
                if(!visible){
                    invisibleSubscriber.add(subscribe);
                    continue;
                }
                try{
                    final Method m=mSubscribes.get(subscribe);
                    Event anno=m.getAnnotation(Event.class);

                    m.setAccessible(true);
                    if(anno.value()) //是否在主线程调用,如果不是进入线程池
                        m.invoke(subscribe,wrapper.obj);
                    else
                        NetManager.sRequestPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    m.invoke(subscribe,wrapper.obj);
                                } catch (IllegalAccessException e) {
                                    Log.w(TAG,"方法调起失败:"+e.getMessage());
                                } catch (InvocationTargetException e) {
                                    Log.w(TAG,"方法调起失败:"+e.getMessage());
                                }
                            }
                        });
                } catch (IllegalAccessException e) {
                    Log.w(TAG,"方法调起失败:"+e.getMessage());
                } catch (InvocationTargetException e) {
                    Log.w(TAG,"方法调起失败:"+e.getMessage());
                }
            }
            for(Object obj:invisibleSubscriber)
                unsubscribe(context,obj);
        }

        /**
         * 检查订阅是否状态正常(这个是不通用的方法)
         * @param subscriber 订阅者
         * @return 如果正常返回true否则false
         */
        private boolean checkVisible(Object subscriber){
            if(subscriber instanceof Activity){
                Activity activity= (Activity) subscriber;
                if(activity.isFinishing())
                    return false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if(activity.isDestroyed())
                        return false;
                }
            }else if(subscriber instanceof Fragment){
                Fragment fragment=(Fragment)subscriber;
                if(fragment.isRemoving()||fragment.isDetached())
                    return false;
            }
            return true;
        }
    }

    public class EntityWrapper implements Serializable {
        public Object obj;
        public EntityWrapper(Object obj){
            this.obj=obj;
        }
    }
}