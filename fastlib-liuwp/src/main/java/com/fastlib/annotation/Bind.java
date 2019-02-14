package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 视图注解，使用在字段上填充字段。使用在方法上接受onClick回调
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bind {
	int[] value(); //要绑定的视图id值
	boolean runOnWorkThread() default false; //是否运行在工作线程中,有返回的方法这个属性无效
	BindType bindType() default BindType.CLICK;

	enum BindType{
		CLICK,
		LONG_CLICK,
		ITEM_CLICK,
		ITEM_LONG_CLICK
	}
}