package com.fastlib.bean;

/**
 * Created by sgfb on 17/5/7.
 * KV数据库使用.List数据表
 */
public class ListTable{
    public int lIndex;
    public String name;
    public String value;

    public ListTable(){}

    public ListTable(int lIndex, String name, String value) {
        this.lIndex = lIndex;
        this.name = name;
        this.value = value;
    }
}