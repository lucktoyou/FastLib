package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create by sgfb on 2019/04/18
 * Modified by liuwp on 2021/3/30.
 * 数据库引用外部文件.该注解对数据库中表对应的实体类的成员变量是String类类型或是json字符串对应的实体类类型有效；其它类型无效。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbFileRef{
    String value() default "";
}
