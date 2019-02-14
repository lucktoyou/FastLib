package com.fastlib.db;

import android.content.Context;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.fastlib.BuildConfig;
import com.fastlib.net.NetManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sgfb on 17/5/8.
 * KV数据库额外功能的子类.默认将数据存储在内存中，仅手动存入外存
 */
public class KVDatabasePlus extends KVDatabase{
    private static Map<String,String> sStrMap;  //字符串缓存
    private static Map<String,Map<String,String>> sHashMap;  //Map缓存
    private static Map<String,List<String>> sListMap;  //列表缓存
    private static Map<String,Set<String>> sSetMap;  //集合缓存

    public KVDatabasePlus(Context context) {
        super(context);
    }

    public KVDatabasePlus(Context context, String databaseName) {
        super(context, databaseName);
    }

    /**
     * 内存中数据存储到外存中
     */
    public void save(){
        if(sStrMap!=null&&!sStrMap.isEmpty()){
            Pair<String,String>[] kvPair=new Pair[sStrMap.size()];
            int index=0;
            for(Map.Entry<String,String> entry:sStrMap.entrySet())
                kvPair[index++]=Pair.create(entry.getKey(),entry.getValue());
            super.setStr(kvPair);
        }
        if(sHashMap!=null&&!sHashMap.isEmpty()){
            for(Map.Entry<String,Map<String,String>> entry:sHashMap.entrySet())
                super.putHash(entry.getKey(),entry.getValue());
        }
        if(sListMap!=null&&!sListMap.isEmpty())
            for(Map.Entry<String,List<String>> entry:sListMap.entrySet())
                super.addToList(entry.getKey(),entry.getValue());
        if(sSetMap!=null&&!sSetMap.isEmpty())
            for(Map.Entry<String,Set<String>> entry:sSetMap.entrySet())
                super.addSet(entry.getKey(),entry.getValue());
    }

    /**
     * 内存中数据存储到外存中，开启异步存储
     */
    public void saveBg(){
        NetManager.sRequestPool.execute(new Runnable() {
            @Override
            public void run() {
                save();
            }
        });
    }

    private void putStrCache(String key,String value){
        if(sStrMap==null)
            sStrMap=new HashMap<>();
        sStrMap.put(key,value);
    }

    private String getStrCache(String key){
        if(sStrMap==null) return null;
        return sStrMap.get(key);
    }

    private void putHashCache(String name,String key,String value){
        if(sHashMap==null)
            sHashMap=new HashMap<>();
        Map<String,String> map=sHashMap.get(name);
        if(map==null)
            sHashMap.put(name,map=new HashMap<>());
        map.put(key,value);
    }

    private void putListCache(String key,String... values){
        if(sListMap==null)
            sListMap=new HashMap<>();
        List<String> currValue=sListMap.get(key);

        if(currValue==null){
            currValue=new ArrayList<>();
            sListMap.put(key,currValue);
        }
        for(String value:values)
            currValue.add(value);
    }

    private List<String> getListCache(String key){
        if(sListMap==null) return null;
        return sListMap.get(key);
    }

    private String getListCacheByIndex(String key,int index){
        if(sListMap==null||!sListMap.containsKey(key)) return key;
        return sListMap.get(key).get(index);
    }

    private Map<String,String> getHashCache(String name){
        if(sHashMap==null) return null;
        return sHashMap.get(name);
    }

    private void putSetCache(String name,String value){
        if(sSetMap==null)
            sSetMap=new HashMap<>();
        Set<String> set=sSetMap.get(name);
        if(set==null){
            set=new HashSet<>();
            sSetMap.put(name,set);
        }
        set.add(value);
    }

    private void putSetCache(String name,Set<String> value){
        if(sSetMap==null)
            sSetMap=new HashMap<>();
        sSetMap.put(name,value);
    }

    private Set<String> getSetCache(String name){
        if(sSetMap==null) return null;
        return sSetMap.get(name);
    }

    @Override
    public String getStr(String key) {
        String cache=getStrCache(key);
        if(cache!=null) return cache;
        String value=super.getStr(key);

        if(value!=null)
            putStrCache(key,value);
        return value;
    }

    @Override
    public boolean setStr(Pair<String, String>... kvPair){
        if(kvPair==null||kvPair.length==0) return false;
        for(Pair<String,String> pair:kvPair)
            putStrCache(pair.first,pair.second);
        return true;
    }

    @Override
    public String getSetStr(String key, String value){
        String oldValue=getStrCache(key);
        if(oldValue!=null){
            putStrCache(key,value);
            return oldValue;
        }
        oldValue=super.getSetStr(key, value);
        if(oldValue!=null)
            putStrCache(key,value);
        return oldValue;
    }

    @Override
    public boolean increStr(String key){
        String value=getStrCache(key);
        if(value!=null){
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
        return super.increStr(key);
    }

    @Override
    public boolean decreStr(String key){
        String value=getStrCache(key);
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
            return false;
        }
        return super.decreStr(key);
    }

    @Override
    public boolean increStr(String key, long incrementCount){
        String value=getStrCache(key);
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
            return false;
        }
        return super.increStr(key, incrementCount);
    }

    @Override
    public boolean decreStr(String key, long decrementCount){
        String value=getStrCache(key);
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
            return false;
        }
        return super.decreStr(key, decrementCount);
    }

    @Override
    public boolean increStr(String key, double incrementCount){
        String value=getStrCache(key);
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
                }
            }
            return false;
        }
        return super.increStr(key, incrementCount);
    }

    @Override
    public boolean decreStr(String key, double decrementCount){
        String value=getStrCache(key);
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
            return false;
        }
        return super.decreStr(key, decrementCount);
    }

    @Override
    public boolean append(String key, String value){
        String cache=getStrCache(key);
        if(cache!=null){
            putStrCache(key,cache.concat(value));
            return true;
        }
        String oldValue=super.getStr(key);
        if(oldValue==null) return false;
        putStrCache(key,oldValue.concat(value));
        return true;
    }

    @Override
    public boolean delHash(String name){
        if(sHashMap!=null&&sHashMap.remove(name)!=null) return true;
        return super.delHash(name);
    }

    @Override
    public boolean delHash(String name, String key) {
        if(sHashMap!=null&&sHashMap.get(name)!=null&&sHashMap.get(name).remove(key)!=null) return true;
        return super.delHash(name,key);
    }

    @Override
    public Map<String, String> getHashMap(String name){
        return getHashCache(name);
    }

    @Override
    public boolean putHash(String name,Map<String, String> map) {
        if(map==null||map.isEmpty()) return false;
        for(Map.Entry<String,String> entry:map.entrySet())
            putHashCache(name,entry.getKey(),entry.getValue());
        return true;
    }

    @Override
    public boolean putHash(String name, String key, String value){
        putHashCache(name,key,value);
        return true;
    }

    @Override
    public String getDataByListIndex(String name, int index){
        return getListCacheByIndex(name,index);
    }

    @Override
    public List<String> getList(String name) {
        return getListCache(name);
    }

    @Override
    public boolean addToList(String name, String... values) {
        putListCache(name,values);
        return true;
    }

    @Override
    public boolean updateLitByIndex(String name, int index, String value){
        String oldValue=getListCacheByIndex(name,index);
        if(oldValue!=null){
            sListMap.get(name).set(index,value);
            return true;
        }
        return super.updateLitByIndex(name,index,value);
    }

    @Override
    public boolean delList(String name){
        if(sListMap!=null&&sListMap.remove(name)!=null) return true;  //如果缓存中有这个值并且移除成功返回true
        return super.delList(name);
    }

    @Override
    public boolean delListByIndex(String name, int index){
        if(sListMap!=null&&sListMap.get(name)!=null&&sListMap.get(name).remove(index)!=null) return true;
        return super.delListByIndex(name, index);
    }

    @Override
    public boolean addSet(String name, String... values){
        for(String value:values)
            putSetCache(name,value);
        return true;
    }

    @Override
    public Set<String> getSet(String name){
        Set<String> cache=getSetCache(name);
        if(cache!=null) return cache;
        Set<String> value=super.getSet(name);
        if(value!=null) putSetCache(name,value);
        return value;
    }

    @Override
    public boolean valueExistsBySet(String name, String value){
        Set<String> cache=getSetCache(name);
        if(cache!=null) return cache.contains(value);
        return super.valueExistsBySet(name,value);
    }

    @Override
    public boolean delSet(String name){
        if(sSetMap!=null&&sSetMap.get(name)!=null) return true;
        return super.delSet(name);
    }

    @Override
    public boolean delSetValue(String name, String... values){
        if(sSetMap!=null&&sSetMap.containsKey(name)){
            for(String value:values)
                if(!sSetMap.get(name).remove(value))
                    return false;
            return true;
        }
        return super.delSetValue(name,values);
    }
}
