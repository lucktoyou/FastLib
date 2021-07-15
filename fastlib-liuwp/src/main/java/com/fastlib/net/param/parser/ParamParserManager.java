package com.fastlib.net.param.parser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.fastlib.net.param.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 2019\12\18.
 * 参数解析器管理
 */
public class ParamParserManager{
    private Map<Class,ParamParser> mMap=new HashMap<>();

    public <T> void putParser(Class<T> cla,ParamParser<T> parser){
        mMap.put(cla,parser);
    }

    public void removeParser(Class cla){
        mMap.remove(cla);
    }

    @SuppressWarnings("unchecked")
    public void parser(boolean duplication, RequestParam requestParam, @Nullable String key, @NonNull Object value){
        ParamParser parser=mMap.get(value.getClass());
        if(parser==null){
            requestParam.getBottomParam().add(Pair.create(key,value));
            return;
        }
        Map<String,List<String>> map=parser.parseParam(key,value);
        if(map==null||map.isEmpty()) {
            return;
        }
        for(Map.Entry<String,List<String>> entry:map.entrySet()){
            if(!requestParam.getSurfaceParam().containsKey(entry.getKey()))
                requestParam.getSurfaceParam().put(entry.getKey(),new ArrayList<String>());
            if(duplication){
                for(String str:entry.getValue())
                    requestParam.getSurfaceParam().get(entry.getKey()).add(str);
            } else{
                requestParam.getSurfaceParam().get(entry.getKey()).clear();
                if(!entry.getValue().isEmpty())
                    requestParam.getSurfaceParam().get(entry.getKey()).add(entry.getValue().get(entry.getValue().size()-1));
            }
        }
    }
}
