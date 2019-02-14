package com.fastlib.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 对象类转换为数据库表信息
 * Created by sgfb on 16/2/15.
 */
public class DatabaseTable{
    public String tableName;
    public String keyFieldName;
    public DatabaseColumn keyColumn;
    public Map<String,DatabaseColumn> columnMap;

    public DatabaseTable(){
        this(null);
    }

    public DatabaseTable(String name){
        tableName=name;
        columnMap=new HashMap<>();
    }

    public static class DatabaseColumn{
        public String columnName;
        public String type;
        public boolean isPrimaryKey;
        public boolean isIgnore;
        public boolean autoincrement;
    }
}