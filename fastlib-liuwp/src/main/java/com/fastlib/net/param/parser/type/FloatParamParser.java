package com.fastlib.net.param.parser.type;

import com.fastlib.net.param.parser.SingleParamParser;

/**
 * Created by sgfb on 2019\12\19.
 */
public class FloatParamParser extends SingleParamParser<Float> {
    @Override
    protected String parseParam(Float param) {
        return Float.toString(param);
    }
}
