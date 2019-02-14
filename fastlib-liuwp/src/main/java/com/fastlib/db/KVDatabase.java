package com.fastlib.db;

import android.content.Context;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.fastlib.BuildConfig;
import com.fastlib.bean.HashMapTable;
import com.fastlib.bean.ListTable;
import com.fastlib.bean.SetTable;
import com.fastlib.bean.StringTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sgfb on 17/5/6.
 * KV数据库封装.旨在替代SharedPreferences并且有更好的使用体验
 */
public class KVDatabase{
    protected Context mContext;
    protected String mDatabaseName;

    public KVDatabase(Context context){
        this(context,FastDatabase.getDefaultDatabaseName());
    }

    public KVDatabase(Context context,String databaseName){
        mContext=context;
        mDatabaseName=databaseName;
    }

    public KVDatabase setWhichDatabase(String databaseName){
        String dbName=databaseName;
        if(TextUtils.isEmpty(dbName))
            dbName=FastDatabase.getDefaultDatabaseName();
        mDatabaseName=dbName;
        return this;
    }

    /**
     * 获取某个字符串映射
     * @param key
     * @return
     */
    public String getStr(String key){
        StringTable st=FastDatabase.getInstance(mContext,mDatabaseName).setFilter(And.condition(Condition.equal(key))).getFirst(StringTable.class);
        return st==null?null:st.value;
    }

    /**
     * 将字符串映射存储到KV数据库中
     * @param kvPair
     * @return
     */
    public boolean setStr(Pair<String,String>... kvPair){
        if(kvPair==null||kvPair.length==0) return false;
        List<StringTable> list=new ArrayList<>(kvPair.length);

        for(Pair<String,String> pair:kvPair)
            list.add(new StringTable(pair.first,pair.second));
        return FastDatabase.getInstance(mContext,mDatabaseName).saveOrUpdate(list);
    }

    /**
     * 将给定的值设置成value，返回旧值
     * @param key
     * @param value
     * @return 旧值
     */
    public String getSetStr(String key,String value){
        String oldValue=getStr(key);
        setStr(Pair.create(key,value));
        return oldValue;
    }

    /**
     * 如果值存在并且为数字，自增1
     * @param key
     * @return
     */
    public boolean increStr(String key){
        String value=getStr(key);
        if(!TextUtils.isEmpty(value)){
            try{
                long longValue=Long.parseLong(value);
                setStr(Pair.create(key,Long.toString(longValue+1)));
                return true;
            }catch (NumberFormatException e){
                try{
                    double doubleValue=Double.parseDouble(value);
                    setStr(Pair.create(key,Double.toHexString(doubleValue+1)));
                    return true;
                }catch (NumberFormatException e1) {
                    if (BuildConfig.DEBUG)
                        e1.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 如果值存在并且为数字，自减1
     * @param key
     * @return
     */
    public boolean decreStr(String key){
        String value=getStr(key);
        if(!TextUtils.isEmpty(value)){
            try{
                long longValue=Long.parseLong(value);
                setStr(Pair.create(key,Long.toString(longValue-1)));
                return true;
            }catch (NumberFormatException e){
                try{
                    double doubleValue=Double.parseDouble(value);
                    setStr(Pair.create(key,Double.toString(doubleValue-1)));
                    return true;
                }catch (NumberFormatException e1){
                    if(BuildConfig.DEBUG)
                        e1.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 如果值存在并且为数字，增加指定增量
     * @param key
     * @param incrementCount 增量
     * @return
     */
    public boolean increStr(String key,long incrementCount){
        String value=getStr(key);
        if(!TextUtils.isEmpty(value)){
            try{
                long numValue=Long.parseLong(value);
                setStr(Pair.create(key,Long.toString(numValue+incrementCount)));
                return true;
            }catch (NumberFormatException e){
                try{
                    double doubleValue=Double.parseDouble(value);
                    setStr(Pair.create(key, Double.toString(doubleValue+incrementCount)));
                    return true;
                }catch (NumberFormatException e1){
                    if(BuildConfig.DEBUG)
                        e1.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 如果值存在并且为数字，减少指定减量
     * @param key
     * @param decrementCount 减量
     * @return
     */
    public boolean decreStr(String key,long decrementCount){
        String value=getStr(key);
        if(!TextUtils.isEmpty(value)){
            try{
                long numValue=Long.parseLong(value);
                setStr(Pair.create(key,Long.toString(numValue-decrementCount)));
                return true;
            }catch (NumberFormatException e){
                try{
                    double doubleValue=Double.parseDouble(value);
                    setStr(Pair.create(key,Double.toString(doubleValue-decrementCount)));
                    return true;
                }catch (NumberFormatException e1){
                    if(BuildConfig.DEBUG)
                        e1.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 如果值存在并且为数字，增加指定增量
     * @param key
     * @param incrementCount 增量
     * @return
     */
    public boolean increStr(String key,double incrementCount){
        String value=getStr(key);
        if(!TextUtils.isEmpty(value)){
            try{
                long longValue=Long.parseLong(value);
                setStr(Pair.create(key,Double.toString(longValue+incrementCount)));
                return true;
            }catch (NumberFormatException e){
                try{
                    double doubleValue=Double.parseDouble(value);
                    setStr(Pair.create(key,Double.toString(doubleValue+incrementCount)));
                    return true;
                }catch (NumberFormatException e1){
                    if(BuildConfig.DEBUG)
                        e1.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 如果值存在并且为数字，减少指定减量
     * @param key
     * @param decrementCount 减量
     * @return
     */
    public boolean decreStr(String key,double decrementCount){
        String value=getStr(key);
        if(!TextUtils.isEmpty(value)){
            try{
                long longValue=Long.parseLong(value);
                setStr(Pair.create(key,Double.toString(longValue-decrementCount)));
                return true;
            }catch (NumberFormatException e){
                try{
                    double doubleValue=Double.parseDouble(value);
                    setStr(Pair.create(key,Double.toString(doubleValue-decrementCount)));
                    return true;
                }catch (NumberFormatException e1){
                    if(BuildConfig.DEBUG)
                        e1.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。
     * @param key
     * @param value
     * @return
     */
    public boolean append(String key,String value){
        String oldValue=getStr(key);
        String newValue=TextUtils.isEmpty(oldValue)?value:oldValue.concat(value);
        return setStr(Pair.create(key,newValue));
    }

    /**
     * 删除指定Hash数据
     * @param name
     * @return
     */
    public boolean delHash(String name){
        return FastDatabase.getInstance(mContext,mDatabaseName).setFilter(And.condition(Condition.equal("name",name))).delete(HashMapTable.class);
    }

    /**
     * 删除指定Hash中的某个键值
     * @param name
     * @param key
     * @return
     */
    public boolean delHash(String name, String key){
        return FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("key",key)))
                .delete(HashMapTable.class);
    }

    /**
     * 获取指定Hash数据
     * @param name
     * @return
     */
    public Map<String,String> getHashMap(String name){
        List<HashMapTable> data=FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)))
                .get(HashMapTable.class);
        if(data==null||data.isEmpty()) return null;
        Map<String,String> map=new HashMap<>();
        for(HashMapTable rowData:data)
            map.put(rowData.key,rowData.value);
        return map;
    }

    /**
     * 存储Hash数据到数据库
     * @param name
     * @param map
     * @return
     */
    public boolean putHash(String name,Map<String,String> map){
        if(map==null||map.isEmpty()) return false;
        boolean success=true;
        for(Map.Entry<String,String> entry:map.entrySet()){
            success&=FastDatabase
                    .getInstance(mContext,mDatabaseName)
                    .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("key",entry.getKey())))
                    .saveOrUpdate(new HashMapTable(name,entry.getKey(),entry.getValue()));
        }
        return success;
    }

    /**
     * 存储Hash数据到数据库
     * @param name
     * @param key
     * @param value
     * @return
     */
    public boolean putHash(String name,String key,String value){
        return FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("key",key)))
                .saveOrUpdate(new HashMapTable(name,key,value));
    }

    /**
     * 获取列表中索引位置的数据
     * @param name
     * @param index
     * @return
     */
    public String getDataByListIndex(String name,int index){
        ListTable lt=FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("lIndex",String.valueOf(index))))
                .getFirst(ListTable.class);
        if(lt!=null) return lt.value;
        return null;
    }

    /**
     * 获取数据库中指定列表
     * @param name
     * @return
     */
    public List<String> getList(String name){
        List<ListTable> data=FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)))
                .get(ListTable.class);
        if(data==null||data.isEmpty()) return null;
        List<String> strList=new ArrayList<>();
        for (ListTable aData : data) strList.add(aData.value);
        return strList;
    }

    /**
     * 增加列表类型数据到数据库
     * @param name
     * @param values
     * @return
     */
    public boolean addToList(String name,String... values){
        int index=0;
        List<ListTable> data=new ArrayList<>(values.length);
        ListTable lastListTable=FastDatabase.getInstance(mContext,mDatabaseName)
                .limit(0,1)
                .orderBy(false,"lIndex")
                .getFirst(ListTable.class);
        if(lastListTable!=null)
            index=lastListTable.lIndex;
        for(String value:values)
            data.add(new ListTable(index++,name,value));
        return FastDatabase.getInstance(mContext,mDatabaseName).saveOrUpdate(data);
    }

    /**
     * 增加列表类型数据到数据库
     * @param name
     * @param list
     * @return
     */
    public boolean addToList(String name,List<String> list){
        if(list==null||list.isEmpty()) return false;
        int index=0;
        List<ListTable> data=new ArrayList<>(list.size());
        ListTable lastListTable=FastDatabase.getInstance(mContext,mDatabaseName)
                .limit(0,1)
                .orderBy(false,"lIndex")
                .getFirst(ListTable.class);
        if(lastListTable!=null)
            index=lastListTable.lIndex;
        for(String value:list)
            data.add(new ListTable(index++,name,value));
        return FastDatabase.getInstance(mContext,mDatabaseName).saveOrUpdate(data);
    }

    /**
     * 修改指定列表类型数据中index位置的数据，如果数据不存在，这次动作会被丢弃
     * @param name
     * @param index
     * @param value
     * @return
     */
    public boolean updateLitByIndex(String name,int index,String value){
        ListTable oldData=FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("lIndex",String.valueOf(index))))
                .getFirst(ListTable.class);
        if(oldData==null) return false;
        return FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("lIndex",String.valueOf(index))))
                .saveOrUpdate(new ListTable(index,name,value));
    }

    /**
     * 从数据库删除指定List
     * @param name
     * @return
     */
    public boolean delList(String name){
        return FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)))
                .delete(ListTable.class);
    }

    /**
     * 从数据库删除指定List位置为index的数据
     * @param name
     * @param index
     * @return
     */
    public boolean delListByIndex(String name,int index){
        return FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("lIndex",String.valueOf(index))))
                .delete(ListTable.class);
    }

    /**
     * 添加数据到指定集合中
     * @param name
     * @param values
     * @return
     */
    public boolean addSet(String name,String... values){
        if(values==null||values.length==0) return false;
        boolean success=true;

        for(String value:values)
            success&=FastDatabase.getInstance(mContext,mDatabaseName)
                    .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("value",value)))
                    .saveOrUpdate(new SetTable(name,value));
        return success;
    }

    public boolean addSet(String name,Set<String> set){
        if(set==null||set.isEmpty()) return false;
        boolean success=true;

        for(String value:set)
            success&=FastDatabase.getInstance(mContext,mDatabaseName)
                    .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("value",value)))
                    .saveOrUpdate(new SetTable(name,value));
        return success;
    }

    /**
     * 获取指定集合
     * @param name
     * @return
     */
    public Set<String> getSet(String name){
        List<SetTable> data=FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)))
                .get(SetTable.class);
        if(data==null||data.isEmpty()) return null;
        Set<String> set=new HashSet<>();
        for(SetTable st:data)
            set.add(st.value);
        return set;
    }

    /**
     * 查询指定值是否存在指定Set中
     * @param name
     * @param value
     * @return
     */
    public boolean valueExistsBySet(String name,String value){
        SetTable st=FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("value",value)))
                .getFirst(SetTable.class);
        return st!=null;
    }

    /**
     * 删除指定Set
     * @param name
     * @return
     */
    public boolean delSet(String name){
        return FastDatabase.getInstance(mContext,mDatabaseName)
                .setFilter(And.condition(Condition.equal("name",name)))
                .delete(SetTable.class);
    }

    /**
     * 删除指定Set中的值
     * @param name
     * @param values
     * @return
     */
    public boolean delSetValue(String name,String... values){
        if(values==null||values.length==0) return false;
        boolean success=true;

        for(String value:values)
            success&=FastDatabase.getInstance(mContext,mDatabaseName)
                    .setFilter(And.condition(Condition.equal("name",name)).and(Condition.equal("value",value)))
                    .delete(SetTable.class);
        return success;
    }
}