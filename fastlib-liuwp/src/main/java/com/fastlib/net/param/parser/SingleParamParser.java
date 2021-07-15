package com.fastlib.net.param.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 2019\12\19.
 * 单参数解析
 */
public abstract class SingleParamParser<T> implements ParamParser<T> {

    protected abstract String parseParam(T param);

    @Override
    public Map<String,List<String>> parseParam(String key, T param) {
        Map<String,List<String>> map=new HashMap<>();
        List<String> list=new ArrayList<>();
        list.add(parseParam(param));
        map.put(key,list);
        return map;
    }
}
