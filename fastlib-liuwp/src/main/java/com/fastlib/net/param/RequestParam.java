package com.fastlib.net.param;

import androidx.core.util.Pair;

import com.fastlib.net.param.parser.ParamParserManager;
import com.fastlib.net.param.parser.type.BooleanParamParser;
import com.fastlib.net.param.parser.type.DoubleParamParser;
import com.fastlib.net.param.parser.type.FloatParamParser;
import com.fastlib.net.param.parser.type.IntParamParser;
import com.fastlib.net.param.parser.type.LongParamParser;
import com.fastlib.net.param.parser.type.StringParamParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 2019\12\24.
 * 网络请求参数容器
 */
public class RequestParam {
    private static ParamParserManager sParamParserManager;                 //参数解析器.影响最终发送时如何传递参数
    private Map<String, List<String>> mSurfaceParam = new HashMap<>();     //表层参数，纯字符串(HashMap对象的key、value值均可为null)
    private List<Pair<String, Object>> mBottomParam = new ArrayList<>();   //里层参数，多类型混合

    static {
        sParamParserManager = new ParamParserManager();
        sParamParserManager.putParser(Integer.class, new IntParamParser());
        sParamParserManager.putParser(Long.class, new LongParamParser());
        sParamParserManager.putParser(Boolean.class, new BooleanParamParser());
        sParamParserManager.putParser(Float.class, new FloatParamParser());
        sParamParserManager.putParser(Double.class, new DoubleParamParser());
        sParamParserManager.putParser(String.class, new StringParamParser());
    }

    public void put(Object value) {
        sParamParserManager.parser(false, this, null, value);
    }

    public void put(String key, Object value) {
        sParamParserManager.parser(false, this, key, value);
    }

    public void add(Object value) {
        sParamParserManager.parser(true, this, null, value);
    }

    public void add(String key, Object value) {
        sParamParserManager.parser(true, this, key, value);
    }

    public Map<String, List<String>> getSurfaceParam() {
        return mSurfaceParam;
    }

    public List<Pair<String, Object>> getBottomParam() {
        return mBottomParam;
    }
}
