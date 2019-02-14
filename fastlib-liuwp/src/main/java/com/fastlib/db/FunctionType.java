package com.fastlib.db;

/**
 * Created by sgfb on 17/7/10.
 * 支持的函数操作类型
 */
public enum FunctionType{
    SUM("sum"),
    AVG("avg"),
    MAX("max"),
    MIN("min");
    private String mName;

    FunctionType(String name){
        mName=name;
    }

    public String getName() {
        return mName;
    }
}
