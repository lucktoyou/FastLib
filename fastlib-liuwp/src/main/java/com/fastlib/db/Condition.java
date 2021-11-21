package com.fastlib.db;

import android.text.TextUtils;

/**
 * Created by sgfb on 17/1/4.
 * 过滤条件.
 */
public final class Condition{
    public static final int TYPE_NULL = 0;
    public static final int TYPE_NOT_NULL = 1;
    public static final int TYPE_BIGGER = 2;
    public static final int TYPE_SMALLER = 3;
    public static final int TYPE_EQUAL = 4;
    public static final int TYPE_UNEQUAL = 5;
    private final int mType;
    private final String mColumn;//列名
    private final String mValue;//列值

    private Condition(int type,String column,String value){
        mType = type;
        mColumn = column;
        mValue = value;
    }

    public static Condition bigger(String column,String value){
        return new Condition(TYPE_BIGGER,column,value);
    }

    public static Condition bigger(String column,int value){
        return bigger(column,Integer.toString(value));
    }

    public static Condition bigger(String column,long value){
        return bigger(column,Long.toString(value));
    }

    public static Condition bigger(String column,float value){
        return bigger(column,Float.toString(value));
    }

    public static Condition bigger(String column,double value){
        return bigger(column,Double.toString(value));
    }

    public static Condition bigger(String value){
        return bigger(null,value);
    }

    public static Condition bigger(int value){
        return bigger(null,Integer.toString(value));
    }

    public static Condition bigger(long value){
        return bigger(null,Long.toString(value));
    }

    public static Condition bigger(float value){
        return bigger(null,Float.toString(value));
    }

    public static Condition bigger(double value){
        return bigger(null,Double.toString(value));
    }

    public static Condition smaller(String column,String value){
        return new Condition(TYPE_SMALLER,column,value);
    }

    public static Condition smaller(String column,int value){
        return smaller(column,Integer.toString(value));
    }

    public static Condition smaller(String column,long value){
        return smaller(column,Long.toString(value));
    }

    public static Condition smaller(String column,float value){
        return smaller(column,Float.toString(value));
    }

    public static Condition smaller(String column,double value){
        return smaller(column,Double.toString(value));
    }

    public static Condition smaller(String value){
        return smaller(null,value);
    }

    public static Condition smaller(int value){
        return smaller(null,Integer.toString(value));
    }

    public static Condition smaller(long value){
        return smaller(null,Long.toString(value));
    }

    public static Condition smaller(float value){
        return smaller(null,Float.toString(value));
    }

    public static Condition smaller(double value){
        return smaller(null,Double.toString(value));
    }

    public static Condition emptyValue(String column){
        return new Condition(TYPE_NULL,column,null);
    }

    public static Condition emptyValue(){
        return emptyValue(null);
    }

    public static Condition notEmptyValue(String column){
        return new Condition(TYPE_NOT_NULL,column,null);
    }

    public static Condition notEmptyValue(){
        return notEmptyValue(null);
    }

    public static Condition equal(String column,String value){
        return new Condition(TYPE_EQUAL,column,value);
    }

    public static Condition equal(String column,int value){
        return equal(column,Integer.toString(value));
    }

    public static Condition equal(String column,long value){
        return equal(column,Long.toString(value));
    }

    public static Condition equal(String column,float value){
        return equal(column,Float.toString(value));
    }

    public static Condition equal(String column,double value){
        return equal(column,Double.toString(value));
    }

    public static Condition equal(String value){
        return equal(null,value);
    }

    public static Condition equal(int value){
        return equal(null,Integer.toString(value));
    }

    public static Condition equal(long value){
        return equal(null,Long.toString(value));
    }

    public static Condition equal(float value){
        return equal(null,Float.toString(value));
    }

    public static Condition equal(double value){
        return equal(null,Double.toString(value));
    }

    public static Condition unequal(String column,String value){
        return new Condition(TYPE_UNEQUAL,column,value);
    }

    public static Condition unequal(String column,int value){
        return unequal(column,Integer.toString(value));
    }

    public static Condition unequal(String column,long value){
        return unequal(column,Long.toString(value));
    }

    public static Condition unequal(String column,float value){
        return unequal(column,Float.toString(value));
    }

    public static Condition unequal(String column,double value){
        return unequal(column,Double.toString(value));
    }

    public static Condition unequal(String value){
        return unequal(null,value);
    }

    public static Condition unequal(int value){
        return unequal(null,Integer.toString(value));
    }

    public static Condition unequal(long value){
        return unequal(null,Long.toString(value));
    }

    public static Condition unequal(float value){
        return unequal(null,Float.toString(value));
    }

    public static Condition unequal(double value){
        return unequal(null,Double.toString(value));
    }

    public int getType(){
        return mType;
    }

    public String getValue(){
        return mValue;
    }

    /**
     * 根据类型转换表达式字符串
     * @param key 主键名
     * @return 表达式字符串
     */
    public String getExpression(String key){
        String columnName = TextUtils.isEmpty(mColumn) ? key : mColumn;
        switch(mType){
            case TYPE_BIGGER:
                return columnName+">?";
            case TYPE_EQUAL:
                return columnName+"=?";
            case TYPE_SMALLER:
                return columnName+"<?";
            case TYPE_UNEQUAL:
                return columnName+"!=?";
            case TYPE_NULL:
                return columnName+" is null";
            case TYPE_NOT_NULL:
                return columnName+" not null";
            default:
                return "";
        }
    }
}