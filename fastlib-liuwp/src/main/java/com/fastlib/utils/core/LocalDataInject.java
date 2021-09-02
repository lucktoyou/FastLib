package com.fastlib.utils.core;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fastlib.annotation.Bind;
import com.fastlib.annotation.LocalData;
import com.fastlib.db.And;
import com.fastlib.db.Condition;
import com.fastlib.db.FastDatabase;
import com.fastlib.utils.FastLog;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sgfb on 17/2/20.
 * Modified by liuwp on 2020/8/13.
 * Modified by liuwp on 2021/9/2.
 * 本地数据注入.
 */
public class LocalDataInject {
    private List<Pair<Field,LocalData>> mChildTriggerFields = new ArrayList<>();  //子Activity返回触发的数据注入字段
    private List<Pair<Method,LocalData>> mChildTriggerMethods = new ArrayList<>();//子Activity返回触发的数据注入方法
    private List<Pair<Method,LocalData>> mDelayTriggerMethods = new ArrayList<>();//延迟触发的数据注入方法
    private SparseArray<Object[]> mTriggerData = new SparseArray<>();             //触发后读取数据缓存点
    private Object mHost;

    public LocalDataInject(@Nullable Object host) {
        if (host != null)
            mHost = host;
    }

    /**
     * 处理从父Activity获取的Intent中包含所需的数据并注入到延迟触发的方法中。
     */
    public void injectDelayTriggerMethod() {
        Bundle bundle = null;

        if (mHost instanceof Activity)
            bundle = ((Activity) mHost).getIntent().getExtras();
        else if (mHost instanceof Fragment)
            bundle = ((Fragment) mHost).getArguments();
        if (bundle == null) return;//如果bundle为空说明当前host不支持这个方法

        for (Pair<Method, LocalData> pair : mDelayTriggerMethods) {
            LocalData ld = pair.second;
            Object[] args = new Object[ld.value().length];

            for (int i = 0; i < args.length; i++) {
                String key = ld.value()[i];
                args[i] = bundle.get(key);
            }
            try {
                pair.first.invoke(mHost, args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理子Activity返回的Intent中包含所需的数据
     *
     * @param data 数据包裹
     */
    public void injectChildBack(Intent data) {
        for (Pair<Field, LocalData> pair : mChildTriggerFields)
            try {
                loadLocalDataFromIntent(data, pair.first, pair.second);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        for (Pair<Method, LocalData> pair : mChildTriggerMethods) {
            if (data == null) return;
            LocalData ld = pair.second;
            Object[] args = new Object[ld.value().length];
            Bundle innerBundle = data.getExtras();
            for (int i = 0; i < ld.value().length; i++) {
                String key = ld.value()[i];
                args[i] = innerBundle.get(key);
            }
            try {
                pair.first.invoke(mHost, args);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 本地数据注入
     */
    public void localDataInject() {
        Field[] fields = mHost.getClass().getDeclaredFields();
        //属性注入
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                LocalData lr = field.getAnnotation(LocalData.class);
                Deprecated deprecated = field.getAnnotation(Deprecated.class);
                if (lr == null || deprecated != null)
                    continue;
                try {
                    switch (lr.from()) {
                        case INTENT_PARENT:
                            loadLocalDataFromIntent(null, field, lr);
                            break;
                        case INTENT_CHILD:
                            mChildTriggerFields.add(new Pair<>(field, lr));
                            break;
                        case SP:
                            loadLocalDataFromSp(field, lr);
                            break;
                        case DATABASE:
                            loadLocalDataFromDatabase(field, lr);
                            break;
                        case ASSETS:
                            loadLocalDataFromFile(field, lr, true);
                            break;
                        case FILE:
                            loadLocalDataFromFile(field, lr, false);
                            break;
                        default:
                            break;
                    }
                } catch (IllegalAccessException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        View rootView = null;

        if (mHost instanceof Activity)
            rootView = ((Activity) mHost).findViewById(android.R.id.content);
        else if (mHost instanceof Fragment)
            rootView = ((Fragment) mHost).getView();
        if (rootView == null) return;
        Method[] methods = mHost.getClass().getDeclaredMethods();
        //"触发数据"注入
        if (methods != null && methods.length > 0) {
            for (final Method m : methods) {
                m.setAccessible(true);
                final LocalData ld = m.getAnnotation(LocalData.class);
                final Bind bind = m.getAnnotation(Bind.class);
                Deprecated deprecated = m.getAnnotation(Deprecated.class);

                if (ld != null && deprecated == null) {
                    if (bind != null) { //视图触发
                        @IdRes final int id = bind.value()[0];
                        View v = rootView.findViewById(id);
                        switch (bind.bindType()) {
                            case CLICK:
                                v.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        invokeTriggerCallback(v, m, ld, bind.bindType(), this, null, null, null);
                                    }
                                });
                                break;
                            case LONG_CLICK:
                                v.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        invokeTriggerCallback(v, m, ld, bind.bindType(), null, this, null, null);
                                        return false;
                                    }
                                });
                                break;
                            case ITEM_CLICK:
                                ((AdapterView<?>) v).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        invokeTriggerCallback(parent, m, ld, bind.bindType(), null, null, this, null);
                                    }
                                });
                                break;
                            case ITEM_LONG_CLICK:
                                ((AdapterView<?>) v).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                    @Override
                                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                        invokeTriggerCallback(parent, m, ld, bind.bindType(), null, null, null, this);
                                        return false;
                                    }
                                });
                        }
                    } else {
                        //延迟触发或子activity返回后触发
                        if (ld.from() == LocalData.GiverType.INTENT_CHILD) {
                            mChildTriggerMethods.add(Pair.create(m, ld));
                        } else {
                            mDelayTriggerMethods.add(Pair.create(m, ld));
                        }
                    }
                }
            }
        }
    }

    private void invokeTriggerCallback(View v,Method m,LocalData ld,Bind.BindType type,View.OnClickListener clickListener,View.OnLongClickListener longClickListtener,
                                       AdapterView.OnItemClickListener itemClickListener,AdapterView.OnItemLongClickListener itemLongClickListener) {
        Object[] data = mTriggerData.get(v.getId());
        Class<?>[] paramTypes = m.getParameterTypes();
        try {
            if (data == null) { //如果没有则读取一份进入缓存
                FastLog.d("缓存中没有触发数据");
                //截断触发事件直到数据读取完毕
                switch (type) {
                    case CLICK:
                        v.setOnClickListener(null);
                        break;
                    case LONG_CLICK:
                        v.setOnLongClickListener(null);
                        break;
                    case ITEM_CLICK:
                        ((AdapterView<?>) v).setOnItemClickListener(null);
                        break;
                    case ITEM_LONG_CLICK:
                        ((AdapterView<?>) v).setOnItemLongClickListener(null);
                        break;
                }
                data = loadLocalData(ld, Arrays.copyOfRange(paramTypes, 1, paramTypes.length));
                switch (type) {
                    case CLICK:
                        v.setOnClickListener(clickListener);
                        break;
                    case LONG_CLICK:
                        v.setOnLongClickListener(longClickListtener);
                        break;
                    case ITEM_CLICK:
                        ((AdapterView<?>) v).setOnItemClickListener(itemClickListener);
                        break;
                    case ITEM_LONG_CLICK:
                        ((AdapterView<?>) v).setOnItemLongClickListener(itemLongClickListener);
                        break;
                }
                mTriggerData.append(v.getId(), data);
            } else{
                FastLog.d("缓存中有触发数据");
            }
            //View必须在第一个，接下来是参数对象数组
            flatInvoke(m, v, data);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void flatInvoke(Method m, View v, Object[] data) throws InvocationTargetException, IllegalAccessException {
        switch (data.length) {
            case 1:
                m.invoke(mHost, v, data[0]);
                break;
            case 2:
                m.invoke(mHost, v, data[0], data[1]);
                break;
            case 3:
                m.invoke(mHost, v, data[0], data[1], data[2]);
                break;
            case 4:
                m.invoke(mHost, v, data[0], data[1], data[2], data[3]);
                break;
            case 5:
                m.invoke(mHost, v, data[0], data[1], data[2], data[3], data[4]);
                break;
            case 6:
                m.invoke(mHost, v, data[0], data[1], data[2], data[3], data[4], data[5]);
                break;
            case 7:
                m.invoke(mHost, v, data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                break;
            case 8:
                m.invoke(mHost, v, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
                break;
            case 9:
                m.invoke(mHost, v, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8]);
            case 10:
                m.invoke(mHost, v, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                break;
            default:
                break;
        }
    }

    /**
     * 读取本地数据，不支持子Activity返回的Intent
     *
     * @param ld
     * @param param
     * @return
     */
    private Object[] loadLocalData(LocalData ld, Class<?>[] param) {
        Object[] datas = new Object[ld.value().length];
        for (int i = 0; i < datas.length; i++) {
            switch (ld.from()) {
                case INTENT_PARENT:
                    datas[i] = loadLocalDataFromIntent(i, ld);
                    break;
                case SP:
                    datas[i] = loadLocalDataFromSp(i, ld);
                    break;
                case FILE:
                    datas[i] = loadLocalDataFromFile(i, ld, param[i], false);
                    break;
                case ASSETS:
                    datas[i] = loadLocalDataFromFile(i, ld, param[i], true);
                    break;
                case DATABASE:
                    datas[i] = loadLocalDataFromDatabase(i, ld, param[i]);
                    break;
            }
        }
        return datas;
    }

    /**
     * 读取数据库中的数据,仅主键过滤
     *
     * @param position
     * @param ld
     * @param paramsType
     * @return
     */
    private Object loadLocalDataFromDatabase(int position, LocalData ld, Class<?> paramsType) {
        return FastDatabase.getDefaultInstance(ContextHolder.getContext(mHost)).setFilter(And.condition(Condition.equal(ld.value()[position]))).getFirst(paramsType);
    }

    /**
     * 读取文件的数据.自动判断类型
     *
     * @param position
     * @param ld
     * @param paramsType
     * @param fromAssets
     * @return
     */
    private Object loadLocalDataFromFile(int position, LocalData ld, Class<?> paramsType, boolean fromAssets) {
        AssetManager am = null;

        if (mHost instanceof Activity) am = ((Activity) mHost).getAssets();
        else if (mHost instanceof Fragment) am = ((Fragment) mHost).getActivity().getAssets();
        if (am == null && fromAssets) return null;

        try {
            byte[] data = fromAssets ? SaveUtil.loadAssetsFile(am, ld.value()[position]) : SaveUtil.loadFile(ld.value()[position]);
            if (data == null)
                return null;
            if (paramsType == byte[].class)
                return data;
            else if (paramsType == String.class)
                return new String(data);
            else {
                Gson gson = new Gson();
                return gson.fromJson(new String(data), paramsType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取SharedPreferences中的数据
     *
     * @param position
     * @param ld
     * @return
     */
    private Object loadLocalDataFromSp(int position, LocalData ld) {
        return SaveUtil.getFromSp(ld.value()[position], null);
    }

    /**
     * 读取在Intent或Bundle中的数据
     *
     * @param position 对应注解下标
     * @param ld       本地数据注解
     * @return 对应包裹里数据
     */
    private Object loadLocalDataFromIntent(int position, LocalData ld) {
        Bundle bundle = null;

        if (mHost instanceof Activity) bundle = ((Activity) mHost).getIntent().getExtras();
        else if (mHost instanceof Fragment) bundle = ((Fragment) mHost).getArguments();
        if (bundle == null) return null;
        return bundle.get(ld.value()[position]);
    }

    /**
     * 从数据库中加载数据到属性中(仅支持FastDatabase)
     *
     * @param field
     * @param lr
     * @throws IllegalAccessException
     */
    private void loadLocalDataFromDatabase(Field field, LocalData lr) throws IllegalAccessException {
        Class<?> type = field.getType();
        Object obj = FastDatabase.getDefaultInstance(ContextHolder.getContext(mHost)).setFilter(And.condition(Condition.equal(lr.value()[0]))).getFirst(type);
        field.set(mHost, obj);
    }

    /**
     * 从SharedPreferences中加载数据到属性中
     *
     * @param field
     * @param lr
     * @throws IllegalAccessException
     */
    private void loadLocalDataFromSp(Field field, LocalData lr) throws IllegalAccessException {
        field.set(mHost, SaveUtil.getFromSp(lr.value()[0], null));
    }

    /**
     * 从文件中加载数据到属性中.
     *
     * @param field
     * @param lr
     * @param fromAssets
     * @throws IOException
     * @throws IllegalAccessException
     */
    private void loadLocalDataFromFile(Field field, LocalData lr, boolean fromAssets) throws IOException, IllegalAccessException {
        AssetManager am = null;

        if (mHost instanceof Activity) am = ((Activity) mHost).getAssets();
        else if (mHost instanceof Fragment) am = ((Fragment) mHost).getActivity().getAssets();
        if (am == null && fromAssets) return;

        byte[] data = fromAssets ? SaveUtil.loadAssetsFile(am, lr.value()[0]) : SaveUtil.loadFile(lr.value()[0]);
        if (data == null)
            return;
        Class<?> type = field.getType();
        if (type == byte[].class)
            field.set(mHost, data);
        else if (type == String.class)
            field.set(mHost, new String(data));
        else {
            Gson gson = new Gson();
            field.set(mHost, gson.fromJson(new String(data), type));
        }
    }

    /**
     * 从Intent中加载本地数据
     *
     * @param field 字段
     * @param lr    本地数据注解
     * @throws IllegalAccessException
     */
    private void loadLocalDataFromIntent(@Nullable Intent childIntent, Field field, LocalData lr) throws IllegalAccessException {
        Bundle bundle = null;

        if (childIntent != null) bundle = childIntent.getExtras();
        else if (mHost instanceof Activity) bundle = ((Activity) mHost).getIntent().getExtras();
        else if (mHost instanceof Fragment) bundle = ((Fragment) mHost).getArguments();
        if (bundle == null) return;

        Object value = bundle.get(lr.value()[0]);
        if (value != null) {
            field.set(mHost, value);
        }
    }
}