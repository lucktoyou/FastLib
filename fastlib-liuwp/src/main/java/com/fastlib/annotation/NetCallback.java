package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create by sgfb on 2019/04/18
 * E-Mail:602687446@qq.com
 * 指定Request返回监听的方法。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NetCallback {

    /**
     * 指定的方法名
     */
    String value();

    /**
     * 参数列表
     */
    Class[] params() default {};
}
