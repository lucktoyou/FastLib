package com.example.fastlibdemo.base;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.viewbinding.ViewBinding;

import com.fastlib.base.module.FastFragment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by liuwp on 2021/1/26.
 */
public abstract class BindViewFragment<T extends ViewBinding> extends FastFragment {
    protected T mViewBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass != null) {
            Class<?> aClass = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
            if (aClass != null) {
                try {
                    Method method = aClass.getDeclaredMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
                    mViewBinding = (T) method.invoke(null, getLayoutInflater(), container, false);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        if (mViewBinding == null)
            return super.onCreateView(inflater, container, savedInstanceState);
        return mViewBinding.getRoot();
    }
}