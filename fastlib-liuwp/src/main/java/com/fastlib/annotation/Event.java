package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sgfb on 16/9/1.
 * 事件方法注解,有这个注解的方法会接受广播来的事件(根据参数来过滤)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Event{
    boolean value() default true; //是否运行在主线程中
}
