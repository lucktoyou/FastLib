package com.fastlib.utils.core;

import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import com.fastlib.annotation.Bind;
import com.fastlib.annotation.LocalData;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liuwp on 2020/8/13.
 * 视图注入到字段和方法中.
 */
public class ViewInject {
    private Object mHost;
    private View mRoot;

    private ViewInject(Object host, @NonNull View root) {
        mHost = host;
        mRoot = root;
        injectViewEvent();
    }

    public static void inject(Object host, @NonNull View root) {
        new ViewInject(host, root);
    }


    private boolean invokeWithoutError(final Method m, final Object... objs) {
        try {
            Object result = m.invoke(mHost);//先尝试绑定无参方法
            if (result instanceof Boolean){
                return (boolean) result;
            }
        } catch (InvocationTargetException e) {//这个异常是非方法参数异常所以直接显示或抛出
            e.printStackTrace();
        }  catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e) {
            try {
                Object result = m.invoke(mHost, objs);
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 绑定方法到事件监听中
     *
     * @param m
     * @param v
     * @param vi
     */
    private void bindListener(final Method m, View v, final Bind vi) {
        switch (vi.bindType()) {
            case CLICK:
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        invokeWithoutError(m, v);
                    }
                });
                break;
            case LONG_CLICK:
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return invokeWithoutError(m, v);
                    }
                });
                break;
            case ITEM_CLICK:
                ((AdapterView<?>) v).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        invokeWithoutError(m, parent, view, position, id);
                    }
                });
                break;
            case ITEM_LONG_CLICK:
                ((AdapterView<?>) v).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        return invokeWithoutError(m, parent, view, position, id);
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * 绑定控件到方法和属性中
     */
    private void injectViewEvent() {
        Class<?> cla = mHost.getClass();
        injectMethod(cla.getDeclaredMethods());
        injectField(cla.getDeclaredFields());
    }

    private void injectMethod(Method[] methods) {
        if (methods != null && methods.length > 0) {
            for (final Method m : methods) {
                try {
                    m.setAccessible(true);
                    Bind vi = m.getAnnotation(Bind.class);
                    LocalData ld = m.getAnnotation(LocalData.class);
                    Deprecated deprecated = m.getAnnotation(Deprecated.class);
                    if (vi != null && ld == null && deprecated == null) {
                        int[] ids = vi.value();

                        if (ids.length > 0) {
                            for (int id : ids) {
                                View v = mRoot.findViewById(id);
                                if (v != null)
                                    bindListener(m, v, vi);
                            }
                        }
                    }
                } catch (NoClassDefFoundError e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void injectField(Field[] fields) {
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                try {
                    Bind vi = field.getAnnotation(Bind.class);
                    Deprecated deprecated = field.getAnnotation(Deprecated.class);
                    if (vi != null && deprecated == null) {
                        int[] ids = vi.value();

                        if (ids.length > 0) {
                            try {
                                View view = mRoot.findViewById(ids[0]);
                                field.setAccessible(true);
                                field.set(mHost, view);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (NoClassDefFoundError e) {
                    e.printStackTrace();
                }
            }
        }
    }
}