package com.fastlib.utils.core;

import androidx.annotation.Nullable;

import com.fastlib.utils.FastLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Modified by liuwp on 2021/11/16.
 * 使反射变得更简单容易
 */
public class Reflect {

    /**
     * 寻找当前类和向上寻找类注解
     *
     * @param cla  类
     * @param anno 指定注解
     * @param <T>  注解类型
     * @return 如果不为空返回指定注解实体
     */
    public static @Nullable
    <T extends Annotation> T findAnnotation(Class<?> cla, Class<T> anno) {
        return findAnnotation(cla, anno, false);
    }

    /**
     * 寻找当前类和向上寻找类注解
     *
     * @param cla           类
     * @param anno          指定注解
     * @param findInterface 是否查看接口注解
     * @param <T>           注解类型
     * @return 如果不为空返回指定注解实体
     */
    public static @Nullable
    <T extends Annotation> T findAnnotation(Class<?> cla, Class<T> anno, boolean findInterface) {
        if (findInterface)
            return findCurrAnnotation(cla, anno);
        else {
            Annotation annoInstance;
            Class<?> upwardClass = cla;
            do {
                annoInstance = upwardClass.getAnnotation(anno);
                if (annoInstance != null) break;
                upwardClass = upwardClass.getSuperclass();
            } while (upwardClass != null);
            return (T) annoInstance;
        }
    }

    private static <T extends Annotation> T findCurrAnnotation(Class<?> cla, Class<T> anno) {
        Annotation annoInstance = cla.getAnnotation(anno);
        if (annoInstance == null) {
            if (cla.getSuperclass() != null) {
                annoInstance = findCurrAnnotation(cla.getSuperclass(), anno);
                if (annoInstance != null) return (T) annoInstance;
            }
            if (cla.getInterfaces().length > 0) {
                for (Class<?> inter : cla.getInterfaces()) {
                    annoInstance = findCurrAnnotation(inter, anno);
                    if (annoInstance != null) return (T) annoInstance;
                }
            }
        }
        return (T) annoInstance;
    }

    ////////////////////////////////////////////////////////////////////////////

    public static boolean isInteger(String type) {
        return type.equals("byte")|| type.equals("short") || type.equals("int") || type.equals("long");
    }

    public static boolean isInteger(Field field) {
        return isInteger(field.getType().getSimpleName());
    }

    public static boolean isReal(String type) {
        return type.equals("float") || type.equals("double");
    }

    public static boolean isReal(Field field) {
        return isReal(field.getType().getSimpleName());
    }

    public static boolean isVarchar(String type) {
        return type.equals("char") || type.equals("java_lang_String") || type.equals("String");
    }

    public static boolean isVarchar(Field field) {
        return isVarchar(field.getType().getSimpleName());
    }

    public static String toSQLType(String type) {
        if (isInteger(type))
            return "integer";
        if (isReal(type))
            return "real";
        if (isVarchar(type))
            return "varchar";
        return type;
    }

    /**
     * 转换成字符串
     *
     * @param obj
     * @return
     */
    public static String objToStr(Object obj) {
        if (obj instanceof Short)
            return Short.toString((short) obj);
        else if (obj instanceof Integer)
            return Integer.toString((int) obj);
        else if (obj instanceof Long)
            return Long.toString((long) obj);
        else if (obj instanceof Float)
            return Float.toString((float) obj);
        else if (obj instanceof Double)
            return Double.toString((double) obj);
        else if (obj instanceof String)
            return (String) obj;
        else
            return obj.toString();
    }

    /**
     * 对比是否同一类型，基本对象与基本类型是同一类型
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean equalBasicOrBasicObj(Class<?> obj1, Class<?> obj2) {
        if (obj1 == byte.class || obj1 == Byte.class)
            return obj2 == byte.class || obj2 == Byte.class;
        if (obj1 == char.class || obj1 == Character.class)
            return obj2 == char.class || obj2 == Character.class;
        if (obj1 == short.class || obj1 == Short.class)
            return obj2 == short.class || obj2 == Short.class;
        if (obj1 == int.class || obj1 == Integer.class)
            return obj2 == int.class || obj2 == Integer.class;
        if (obj1 == long.class || obj1 == Long.class)
            return obj2 == long.class || obj2 == Long.class;
        if (obj1 == float.class || obj1 == Float.class)
            return obj2 == float.class || obj2 == Float.class;
        if (obj1 == double.class || obj1 == Double.class)
            return obj2 == double.class || obj2 == Double.class;
        return obj1 == obj2;
    }
}