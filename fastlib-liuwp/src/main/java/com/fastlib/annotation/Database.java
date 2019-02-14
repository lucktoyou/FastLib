package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * notnull和defaultValue两个值暂时不使用
 */
@Target({ElementType.FIELD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Database {
	
	//数据库中列名
	String columnName() default "";
	
	//不存入数据库
	boolean ignore() default false;
	
	//是否主键
	boolean keyPrimary() default false;
	
	//自动增长，如果不是主键，将自动忽视
	boolean autoincrement() default false;

	//是否允许null
	boolean notNull() default false;

	//默认值
	String defaultValue() default "";
}
