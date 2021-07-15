package com.example.fastlibdemo.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fastlib.base.module.FastActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by liuwp on 2021/1/26.
 */
public abstract class BindViewActivity<T extends ViewBinding> extends FastActivity {
    protected T mViewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Type superclass = getClass().getGenericSuperclass();
        if (superclass != null) {
            Class<?> aClass = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
            try {
                Method method = aClass.getDeclaredMethod("inflate", LayoutInflater.class);
                mViewBinding = (T) method.invoke(null, getLayoutInflater());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (mViewBinding == null) return;
        setContentView(mViewBinding.getRoot());
    }
}