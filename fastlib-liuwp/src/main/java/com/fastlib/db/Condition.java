package com.fastlib.db;

import android.text.TextUtils;

/**
 * Created by sgfb on 17/1/4.
 * 过滤条件
 */
public final class Condition {
    public static final int TYPE_NULL=0;
    public static final int TYPE_NOT_NULL=1;
    public static final int TYPE_BIGGER =2;
    public static final int TYPE_SMALLER=3;
    public static final int TYPE_EQUAL=4;
    public static final int TYPE_UNEQUAL=5;
    private int mType;
    private String mField;
    private String mValue;

    private Condition(int type, String value){
        this(type,null,value);
    }

    private Condition(int type, String field, String value){
        mType=type;
        mField=field;
        mValue=value;
    }

    public static Condition bigger(String value){
        return bigger(null,value);
    }

    public static Condition bigger(int value){
        return bigger(Integer.toString(value));
    }

    public static Condition bigger(long value){
        return bigger(Long.toString(value));
    }

    public static Condition bigger(float value){
        return bigger(Float.toString(value));
    }

    public static Condition bigger(double value){
        return bigger(Double.toString(value));
    }

    public static Condition bigger(String field, int value){
        return bigger(field,Integer.toString(value));
    }

    public static Condition bigger(String field, long value){
        return bigger(field,Long.toString(value));
    }

    public static Condition bigger(String field, float value){
        return bigger(field,Float.toString(value));
    }

    public static Condition bigger(String field, double value){
        return bigger(field,Double.toString(value));
    }

    public static Condition bigger(String field, String value){
        return new Condition(TYPE_BIGGER,field,value);
    }

    public static Condition smaller(String value){
        return smaller(null,value);
    }

    public static Condition smaller(int value){
        return smaller(Integer.toString(value));
    }

    public static Condition smaller(long value){
        return smaller(Long.toString(value));
    }

    public static Condition smaller(float value){
        return smaller(Float.toString(value));
    }

    public static Condition smaller(double value){
        return smaller(Double.toString(value));
    }

    public static Condition smaller(String field, String value){
        return new Condition(TYPE_SMALLER,field,value);
    }

    public static Condition smaller(String field,int value){
        return smaller(field,Integer.toString(value));
    }

    public static Condition smaller(String field,long value){
        return smaller(field,Long.toString(value));
    }

    public static Condition smaller(String field,float value){
        return smaller(field,Float.toString(value));
    }

    public static Condition smaller(String field,double value){
        return smaller(field,Double.toString(value));
    }

    public static Condition emptyVaue(){
        return new Condition(TYPE_NULL,null);
    }

    public static Condition emptyValue(String field){
        return new Condition(TYPE_NULL,field,null);
    }

    public static Condition notEmptyValue(){
        return new Condition(TYPE_NOT_NULL,null);
    }

    public static Condition notEmptyValue(String field){
        return new Condition(TYPE_NOT_NULL,field,null);
    }

    public static Condition equal(String value){
        return equal(null,value);
    }

    public static Condition equal(int value){
        return equal(Integer.toString(value));
    }

    public static Condition equal(long value){
        return equal(Long.toString(value));
    }

    public static Condition equal(float value){
        return equal(Float.toString(value));
    }

    public static Condition equal(double value){
        return equal(Double.toString(value));
    }

    public static Condition equal(String field, String value){
        return new Condition(TYPE_EQUAL,field,value);
    }

    public static Condition equal(String field,int value){
        return equal(field,Integer.toString(value));
    }

    public static Condition equal(String field,long value){
        return equal(field,Long.toString(value));
    }

    public static Condition equal(String field,float value){
        return equal(field,Float.toString(value));
    }

    public static Condition equal(String field,double value){
        return equal(field,Double.toString(value));
    }

    public static Condition unequal(String value){
        return new Condition(TYPE_UNEQUAL,value);
    }

    public static Condition unequal(String field, String value){
        return new Condition(TYPE_UNEQUAL,field,value);
    }

    public static Condition unequal(String field,int value){
        return unequal(field,Integer.toString(value));
    }

    public static Condition unequal(String field,long value){
        return unequal(field,Long.toString(value));
    }

    public static Condition unequal(String field,float value){
        return unequal(field,Float.toString(value));
    }

    public static Condition unequal(String field,double value){
        return unequal(field,Double.toString(value));
    }

    public int getType() {
        return mType;
    }

    public String getValue() {
        return mValue;
    }

    /**
     * 根据类型转换表达式字符串
     * @param key 主键名
     * @return 表达式字符串
     */
    public String getExpression(String key){
        String fieldName= TextUtils.isEmpty(mField)?key:mField;
        switch(mType){
            case TYPE_BIGGER:
                return fieldName+">?";
            case TYPE_EQUAL:
                return fieldName+"=?";
            case TYPE_SMALLER:
                return fieldName+"<?";
            case TYPE_UNEQUAL:
                return fieldName+"!=?";
            case TYPE_NULL:
                return fieldName+" is null";
            case TYPE_NOT_NULL:
                return fieldName+" not null";
            default:return "";
        }
    }
}