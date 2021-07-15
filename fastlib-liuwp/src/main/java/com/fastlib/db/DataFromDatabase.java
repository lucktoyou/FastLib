package com.fastlib.db;

/**
 * Created by sgfb on 17/3/23.
 * Modified by liuwp on 2021/6/23.
 * 从FastDatabase中取数据时，如果遇到自定义构造生成的对象时，作为指定数据库数据替换对象.
 * 例：
 * FastDatabase.getDefaultInstance(LibraryActivity.this)
 * .setConstructorParams(new Object[]{DataFromDatabase.from("name"), DataFromDatabase.from("age"),DataFromDatabase.from("intro")})
 * .get(PersonBeen.class);
 */
public final class DataFromDatabase{
    private String mField;

    private DataFromDatabase(String field){
        mField = field;
    }

    public static DataFromDatabase from(String field){
        return new DataFromDatabase(field);
    }

    public String getField(){
        return mField;
    }

    public void setField(String field){
        mField = field;
    }
}
