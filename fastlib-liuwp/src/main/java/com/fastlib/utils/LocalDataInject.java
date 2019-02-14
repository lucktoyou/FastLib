package com.fastlib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;

import com.fastlib.annotation.Bind;
import com.fastlib.annotation.LocalData;
import com.fastlib.db.And;
import com.fastlib.db.FastDatabase;
import com.fastlib.db.Condition;
import com.fastlib.db.SaveUtil;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by sgfb on 17/2/20.
 * 本地数据注入
 */
public class LocalDataInject{
    private Activity mActivity;
    private Fragment mFragment;
    private List<Pair<Field,LocalData>> mChildActivityGiver = new ArrayList<>(); //子Activity返回时获取Intent中数据
    private SparseArray<Object[]> mToggleData = new SparseArray<>(); //触发后读取数据缓存点

    public LocalDataInject(Activity activity){
        mActivity=activity;
    }

    public LocalDataInject(Fragment fragment){
        mFragment=fragment;
    }

    /**
     * 处理子Activity返回的Intent中包含所需的数据
     * @param data
     */
    public void injectChildBack(Intent data){
        for (Pair<Field, LocalData> pair : mChildActivityGiver)
            try {
                loadLocalDataFromIntent(data,pair.first, pair.second);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
    }

    /**
     * 本地数据注入
     */
    public void localDataInject(){
        Object host=mActivity==null?mFragment:mActivity;
        Field[] fields = host.getClass().getDeclaredFields();
        Method[] methods = host.getClass().getDeclaredMethods();
        //属性注入
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                LocalData lr = field.getAnnotation(LocalData.class);
                if (lr == null)
                    continue;
                try {
                    switch (lr.from()[0]) {
                        case INTENT_PARENT:
                            loadLocalDataFromIntent(null,field, lr);
                            break;
                        case INTENT_CHILD:
                            mChildActivityGiver.add(new Pair<>(field, lr));
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
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //"触发数据"注入
        if (methods != null && methods.length > 0) {
            for (final Method m : methods) {
                m.setAccessible(true);
                final LocalData ld = m.getAnnotation(LocalData.class);
                final Bind bind = m.getAnnotation(Bind.class);
                if (ld != null && bind != null){
                    View v = mActivity==null?mFragment.getView().findViewById(bind.value()[0]):mActivity.findViewById(bind.value()[0]);
                    switch (bind.bindType()) {
                        case CLICK:
                            v.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    invokeToggleCallback(v, m, ld, bind.bindType(), this, null, null, null);
                                }
                            });
                            break;
                        case LONG_CLICK:
                            v.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    invokeToggleCallback(v, m, ld, bind.bindType(), null, this, null, null);
                                    return false;
                                }
                            });
                            break;
                        case ITEM_CLICK:
                            ((AdapterView) v).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    invokeToggleCallback(parent, m, ld, bind.bindType(), null, null, this, null);
                                }
                            });
                            break;
                        case ITEM_LONG_CLICK:
                            ((AdapterView) v).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                    invokeToggleCallback(parent, m, ld, bind.bindType(), null, null, null, this);
                                    return false;
                                }
                            });
                    }
                }
            }
        }
    }

    private void invokeToggleCallback(View v, Method m, LocalData ld, Bind.BindType type, View.OnClickListener clickListener, View.OnLongClickListener longClickListtener,
                                      AdapterView.OnItemClickListener itemClickListener, AdapterView.OnItemLongClickListener itemLongClickListener) {
        Object[] data = mToggleData.get(v.getId());
        Class<?>[] paramTypes = m.getParameterTypes();
        try {
            if (data == null) { //如果没有则读取一份进入缓存
                System.out.println("缓存中没有触发数据");
                //截断触发事件直到数据读取完毕
                switch (type) {
                    case CLICK:
                        v.setOnClickListener(null);
                        break;
                    case LONG_CLICK:
                        v.setOnLongClickListener(null);
                        break;
                    case ITEM_CLICK:
                        ((AdapterView) v).setOnItemClickListener(null);
                        break;
                    case ITEM_LONG_CLICK:
                        ((AdapterView) v).setOnItemLongClickListener(null);
                        break;
                }
                data = loadLocalData(ld, Arrays.copyOfRange(paramTypes,1,paramTypes.length));
                switch (type) {
                    case CLICK:
                        v.setOnClickListener(clickListener);
                        break;
                    case LONG_CLICK:
                        v.setOnLongClickListener(longClickListtener);
                        break;
                    case ITEM_CLICK:
                        ((AdapterView) v).setOnItemClickListener(itemClickListener);
                        break;
                    case ITEM_LONG_CLICK:
                        ((AdapterView) v).setOnItemLongClickListener(itemLongClickListener);
                        break;
                }
                mToggleData.append(v.getId(), data);
            } else
                System.out.println("缓存中有触发数据");
            //View必须在第一个，接下来是参数对象数组
            flatInvoke(m, v, data);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void flatInvoke(Method m, View v, Object[] data) throws InvocationTargetException, IllegalAccessException {
        Object host=mActivity==null?mFragment:mActivity;
        switch (data.length){
            case 1:
                m.invoke(host, v, data[0]);
                break;
            case 2:
                m.invoke(host, v, data[0], data[1]);
                break;
            case 3:
                m.invoke(host, v, data[0], data[1], data[2]);
                break;
            case 4:
                m.invoke(host, v, data[0], data[1], data[2], data[3]);
                break;
            case 5:
                m.invoke(host, v, data[0], data[1], data[2], data[3], data[4]);
                break;
            case 6:
                m.invoke(host, v, data[0], data[1], data[2], data[3], data[4], data[5]);
                break;
            case 7:
                m.invoke(host, v, data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                break;
            case 8:
                m.invoke(host, v, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
                break;
            case 9:
                m.invoke(host, v, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8]);
            case 10:
                m.invoke(host, v, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                break;
            default:
                break;
        }
    }

    /**
     * 读取本地数据，不支持子Activity返回的Intent
     * @param ld
     * @param param
     * @return
     */
    private Object[] loadLocalData(LocalData ld,Class<?>[] param) {
        Object[] datas = new Object[ld.value().length];
        for (int i = 0; i < datas.length; i++) {
            switch (ld.from()[i]){
                case INTENT_PARENT:
                    datas[i] = loadLocalDataFromIntent(i, ld,param[i]);
                    break;
                case SP:
                    datas[i] = loadLocalDataFromSp(i, ld, param[i]);
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
     * @param position
     * @param ld
     * @param paramsType
     * @return
     */
    private Object loadLocalDataFromDatabase(int position, LocalData ld, Class<?> paramsType){
        Context host=mActivity==null?mFragment.getActivity():mActivity;
        return FastDatabase.getDefaultInstance(host).setFilter(And.condition(Condition.equal(ld.value()[position]))).getFirst(paramsType);
    }

    /**
     * 读取外磁卡中或者Assets中的文件的数据.自动判断类型
     * @param position
     * @param ld
     * @param paramsType
     * @param fromAssets
     * @return
     */
    private Object loadLocalDataFromFile(int position, LocalData ld, Class<?> paramsType, boolean fromAssets){
        try {
            Gson gson = new Gson();
            byte[] data = fromAssets ? SaveUtil.loadAssetsFile(mActivity==null?mFragment.getActivity().getAssets():mActivity.getAssets(), ld.value()[position]) : SaveUtil.loadFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + ld.value()[position]);
            if (paramsType == byte[].class)
                return data;
            else if (paramsType == String.class)
                return new String(data);
            else
                return gson.fromJson(new String(data), paramsType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取SharedPreferences中的数据
     * @param position
     * @param ld
     * @param paramType
     * @return
     */
    private Object loadLocalDataFromSp(int position, LocalData ld, Class<?> paramType) {
        Activity host=mActivity==null?mFragment.getActivity():mActivity;
        SharedPreferences sp = host.getSharedPreferences("fastlib",Context.MODE_PRIVATE);
        if (paramType == boolean.class || paramType == Boolean.class)
            return sp.getBoolean(ld.value()[position], false);
        else if (paramType == int.class || paramType == Integer.class)
            return sp.getInt(ld.value()[position], -1);
        else if (paramType == long.class || paramType == Long.class)
            return sp.getLong(ld.value()[position], -1);
        else if (paramType == float.class || paramType == Float.class)
            return sp.getFloat(ld.value()[position], -1.1f);
        else if (paramType == String.class)
            return sp.getString(ld.value()[position], null);
        else if (paramType == Set.class)
            return sp.getStringSet(ld.value()[position], null);
        else
            return null;
    }

    /**
     * 读取在Intent或Bundle中的数据
     * @param position
     * @param ld
     * @param paramType
     * @return
     */
    private Object loadLocalDataFromIntent(int position, LocalData ld, Class<?> paramType){
        Intent intent=mActivity!=null?mActivity.getIntent():null;
        Bundle bundle=mActivity==null?mFragment.getArguments():null;
        if (paramType == boolean.class)
            return intent!=null?intent.getBooleanExtra(ld.value()[position],false):bundle.getBoolean(ld.value()[position],false);
        else if (paramType == boolean[].class)
            return intent!=null?intent.getBooleanArrayExtra(ld.value()[position]):bundle.getBooleanArray(ld.value()[position]);
        else if (paramType == byte.class)
            return intent!=null?intent.getByteExtra(ld.value()[position], (byte) -1):bundle.getByte(ld.value()[position],(byte)-1);
        else if (paramType == byte[].class)
            return intent!=null?intent.getByteArrayExtra(ld.value()[position]):bundle.getByteArray(ld.value()[position]);
        else if (paramType == char.class)
            return intent!=null?intent.getCharExtra(ld.value()[position],'0'):bundle.getChar(ld.value()[position],'0');
        else if (paramType == char[].class)
            return intent!=null?intent.getCharArrayExtra(ld.value()[position]):bundle.getCharArray(ld.value()[position]);
        else if (paramType == short.class)
            return intent!=null?intent.getShortExtra(ld.value()[position], (short) -1):bundle.getShort(ld.value()[position],(short)-1);
        else if (paramType == short[].class)
            return intent!=null?intent.getShortArrayExtra(ld.value()[position]):bundle.getShortArray(ld.value()[position]);
        else if (paramType == int.class)
            return intent!=null?intent.getIntExtra(ld.value()[position], -1):bundle.getInt(ld.value()[position],-1);
        else if (paramType == int[].class)
            return intent!=null?intent.getIntArrayExtra(ld.value()[position]):bundle.getIntArray(ld.value()[position]);
        else if (paramType == float.class)
            return intent!=null?intent.getFloatExtra(ld.value()[position], -1.1f):bundle.getFloat(ld.value()[position],-1.1f);
        else if (paramType == float[].class)
            return intent!=null?intent.getFloatArrayExtra(ld.value()[position]):bundle.getFloatArray(ld.value()[position]);
        else if (paramType == double.class)
            return intent!=null?intent.getDoubleExtra(ld.value()[position], -1.1):bundle.getDouble(ld.value()[position],-1.1);
        else if (paramType == double[].class)
            return intent!=null?intent.getDoubleArrayExtra(ld.value()[position]):bundle.getDoubleArray(ld.value()[position]);
        else if (paramType == String.class)
            return intent!=null?intent.getStringExtra(ld.value()[position]):bundle.getString(ld.value()[position],"-1");
        else if (paramType == String[].class)
            return intent!=null?intent.getStringArrayExtra(ld.value()[position]):bundle.getStringArray(ld.value()[position]);
        else
            return intent!=null?intent.getSerializableExtra(ld.value()[position]):bundle.getStringArray(ld.value()[position]);
    }

    /**
     * 从数据库中加载数据到属性中(仅支持FastDatabase)
     *
     * @param field
     * @param lr
     * @throws IllegalAccessException
     */
    private void loadLocalDataFromDatabase(Field field, LocalData lr) throws IllegalAccessException{
        Context host=mActivity==null?mFragment.getActivity():mActivity;
        Class<?> type = field.getType();
        Object obj = FastDatabase.getDefaultInstance(host).setFilter(And.condition(Condition.equal(lr.value()[0]))).getFirst(type);
        field.set(host,obj);
    }

    /**
     * 从SharedPreferences中加载数据到属性中
     * @param field
     * @param lr
     * @throws IllegalAccessException
     */
    private void loadLocalDataFromSp(Field field, LocalData lr) throws IllegalAccessException{
        Activity host=mActivity==null?mFragment.getActivity():mActivity;
        SharedPreferences sp =host.getSharedPreferences("fastlib",Context.MODE_PRIVATE);
        Class<?> type = field.getType();
        if (type == boolean.class)
            field.setBoolean(host, sp.getBoolean(lr.value()[0], false));
        else if (type == Boolean.class)
            field.set(host, sp.getBoolean(lr.value()[0], false));
        else if (type == int.class)
            field.setInt(host, sp.getInt(lr.value()[0], -1));
        else if (type == Integer.class)
            field.set(host, sp.getInt(lr.value()[0], -1));
        else if (type == float.class)
            field.setFloat(host, sp.getFloat(lr.value()[0], -1));
        else if (type == Float.class)
            field.set(host, sp.getFloat(lr.value()[0], -1));
        else if (type == long.class)
            field.setLong(host, sp.getLong(lr.value()[0], -1));
        else if (type == Long.class)
            field.set(host, sp.getLong(lr.value()[0], -1));
        else if (type == String.class)
            field.set(host, sp.getString(lr.value()[0], null));
    }

    /**
     * 从文件中加载数据到属性中.取文件时默认取外磁卡位置
     * @param field
     * @param lr
     * @param fromAssets
     * @throws IOException
     * @throws IllegalAccessException
     */
    private void loadLocalDataFromFile(Field field, LocalData lr, boolean fromAssets) throws IOException, IllegalAccessException{
        Activity host=mActivity==null?mFragment.getActivity():mActivity;
        byte[] data = fromAssets ? SaveUtil.loadAssetsFile(host.getAssets(), lr.value()[0]) : SaveUtil.loadFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + lr.value()[0]);
        if (data == null)
            return;
        Class<?> type = field.getType();
        if (type == byte[].class)
            field.set(host, data);
        else if (type == String.class)
            field.set(host, new String(data));
        else {
            Gson gson = new Gson();
            field.set(host, gson.fromJson(new String(data), type));
        }
    }

    /**
     * 从Intent中加载本地数据
     * @param field
     * @param lr
     * @throws IllegalAccessException
     */
    private void loadLocalDataFromIntent(@Nullable Intent childIntent, Field field, LocalData lr) throws IllegalAccessException{
        Object host=mActivity==null?mFragment:mActivity;
        Intent intent=childIntent;
        Bundle bundle=mFragment!=null?mFragment.getArguments():null;
        if(intent==null)
            intent=mActivity!=null?mActivity.getIntent():null;
        Class<?> type = field.getType();
        if (type == byte.class)
            field.setByte(host,intent!=null?intent.getByteExtra(lr.value()[0], (byte) -1):bundle.getByte(lr.value()[0],(byte)-1));
        else if (type == Byte.class)
            field.set(host, intent!=null?intent.getByteExtra(lr.value()[0], (byte) -1):bundle.getByte(lr.value()[0],(byte)-1));
        else if (type == byte[].class)
            field.set(host, intent!=null?intent.getByteArrayExtra(lr.value()[0]):bundle.getByteArray(lr.value()[0]));
        else if (type == char.class)
            field.set(host, intent!=null?intent.getCharExtra(lr.value()[0],'0'):bundle.getChar(lr.value()[0],'0'));
        else if (type == char[].class)
            field.set(host, intent!=null?intent.getCharArrayExtra(lr.value()[0]):bundle.getCharArray(lr.value()[0]));
        else if (type == boolean.class)
            field.setBoolean(host,intent!=null?intent.getBooleanExtra(lr.value()[0], false):bundle.getBoolean(lr.value()[0],false));
        else if (type == Boolean.class)
            field.set(host, intent!=null?intent.getBooleanExtra(lr.value()[0], false):bundle.getBoolean(lr.value()[0],false));
        else if (type == boolean[].class)
            field.set(host,intent!=null?intent.getBooleanArrayExtra(lr.value()[0]):bundle.getBooleanArray(lr.value()[0]));
        else if (type == short.class)
            field.set(host, intent!=null?intent.getShortExtra(lr.value()[0], (short) -1):bundle.getShort(lr.value()[0],(short)-1));
        else if (type == Short.class)
            field.set(host,intent!=null?intent.getShortExtra(lr.value()[0], (short) -1):bundle.getShort(lr.value()[0],(short)-1));
        else if (type == short[].class)
            field.set(host, intent!=null?intent.getShortArrayExtra(lr.value()[0]):bundle.getShortArray(lr.value()[0]));
        else if (type == int.class)
            field.setInt(host, intent!=null?intent.getIntExtra(lr.value()[0],-1):bundle.getInt(lr.value()[0],-1));
        else if (type == Integer.class)
            field.set(host, intent!=null?intent.getIntExtra(lr.value()[0], -1):bundle.getInt(lr.value()[0],-1));
        else if (type == int[].class)
            field.set(host, intent!=null?intent.getIntArrayExtra(lr.value()[0]):bundle.getShortArray(lr.value()[0]));
        else if (type == float.class)
            field.setFloat(host,intent!=null?intent.getFloatExtra(lr.value()[0], -1):bundle.getFloat(lr.value()[0],-1.1f));
        else if (type == Float.class)
            field.set(host, intent!=null?intent.getFloatExtra(lr.value()[0], -1):bundle.getFloat(lr.value()[0],-1.1f));
        else if (type == float[].class)
            field.set(host, intent!=null?intent.getFloatArrayExtra(lr.value()[0]):bundle.getFloatArray(lr.value()[0]));
        else if (type == long.class)
            field.setLong(host,intent!=null?intent.getLongExtra(lr.value()[0], -1):bundle.getLong(lr.value()[0],-1));
        else if (type == Long.class)
            field.set(host, intent!=null?intent.getLongExtra(lr.value()[0], -1):bundle.getLong(lr.value()[0],-1));
        else if (type == long[].class)
            field.set(host, intent!=null?intent.getLongArrayExtra(lr.value()[0]):bundle.getLongArray(lr.value()[0]));
        else if (type == double.class)
            field.setDouble(host,intent!=null?intent.getDoubleExtra(lr.value()[0],-1):bundle.getDouble(lr.value()[0],-1.1));
        else if (type == Double.class)
            field.setDouble(host,intent!=null?intent.getDoubleExtra(lr.value()[0], -1):bundle.getDouble(lr.value()[0],-1.1));
        else if (type == double[].class)
            field.set(host, intent!=null?intent.getDoubleArrayExtra(lr.value()[0]):bundle.getDoubleArray(lr.value()[0]));
        else if (type == String.class)
            field.set(host, intent!=null?intent.getStringExtra(lr.value()[0]):bundle.getString(lr.value()[0],"-1"));
        else
            field.set(host, intent!=null?intent.getSerializableExtra(lr.value()[0]):bundle.getSerializable(lr.value()[0]));
    }
}