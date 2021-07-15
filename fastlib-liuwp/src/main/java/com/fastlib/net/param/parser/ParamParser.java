package com.fastlib.net.param.parser;

import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 2019/12/18
 * 解析请求参数的非基本类型字段
 * @param <T>   解析类型
 */
public interface ParamParser<T>{

    /**
     * 尝试解析参数
     * @param key 键
     * @param param 值
     */
    Map<String,List<String>> parseParam(String key, T param);
}