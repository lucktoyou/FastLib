package com.fastlib.bean;

/**
 * Created by sgfb on 17/5/5.
 * KV数据库使用.HashMap数据表
 */
public class HashMapTable{
    public String name;
    public String key;
    public String value;

    public HashMapTable() {
    }

    public HashMapTable(String name, String key, String value) {
        this.name = name;
        this.key = key;
        this.value = value;
    }
}