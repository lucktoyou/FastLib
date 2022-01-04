package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liuwp on 2020/8/13.
 * Modified by liuwp on 2022/1/4.
 * 视图注解，使用在成员变量上绑定视图，使用在成员方法上接受onClick回调。
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bind {
    int[] value();//要绑定的视图id值
    BindType bindType() default BindType.CLICK;

    enum BindType {
        CLICK,
        LONG_CLICK,
        ITEM_CLICK,
        ITEM_LONG_CLICK
    }
}