package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sgfb on 17/2/15.
 * Modified by liuwp on 2020/8/13.
 * 本地一些数据注入.Intent(包括父Activity给予和子Activity返回的Intent中数据),SharedPreferences,数据库,文件(Assets和绝对路径文件).
 * 如果被注入对象是一个方法那么在数据被延迟读入或者"被触发后"执行给予的事件
 * 为防止重复读取外存,"被触发后读取的数据"将被保存到某模块中（Activity或者Fragment或者其他可以称为模块的地方）在模块结束后被释放
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalData{
    String[] value(); //对应Intent和SP中key，database中主键值，Assets和文件的路径.特殊的多键值对应方法触发
    GiverType from() default GiverType.INTENT_PARENT;

    /**
     * 数据来源
     */
    enum GiverType{
        INTENT_PARENT, //父Activity的Intent中数据
        INTENT_CHILD, //子Activity的Intent中数据
        SP, //SharedPreferences中数据
        DATABASE, //数据库中数据（仅支持FastDatabase创建的默认数据库）
        ASSETS, //Assets中数据
        FILE //文件中数据
    }
}