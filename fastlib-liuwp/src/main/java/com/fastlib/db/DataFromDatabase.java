package com.fastlib.db;

/**
 * Created by sgfb on 17/3/23.
 * 从FastDatabase中取数据时，如果遇到自定义构造生成的对象时，作为指定数据库数据替换对象.
 * 如果要实例化如下类
 * public class MyClass{
 *     public int mA;
 *     public String mB;
 *
 *     public MyClass(int a,String b){
 *         mA=a;
 *         mB=b;
 *     }
 * }
 * 假定a取常量x，b从取数据库中取值,那么设定
 * List<Object> constructorParams=new ArrayList<Object>();
 * constructorParams.add(1);
 * constructorParams.add(DataFromDatabase.from("mB"));
 * 这两个参数给予RuntimeAttribute即可
 */
public final class DataFromDatabase{
    private String mField;

    private DataFromDatabase(String field) {
        mField = field;
    }

    public static DataFromDatabase from(String field){
        return new DataFromDatabase(field);
    }


    public String getField() {
        return mField;
    }

    public void setField(String field) {
        mField = field;
    }
}
