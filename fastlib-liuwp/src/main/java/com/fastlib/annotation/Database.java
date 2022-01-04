package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Modified by liuwp on 2021/7/14.
 * Modified by liuwp on 2022/1/4.
 * 1，在实体类的成员变量上注解有效。
 * 2，在实体类中嵌套类的成员变量上注解无效。
 * 3，notNull和defaultValue两个值暂时不使用。
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
