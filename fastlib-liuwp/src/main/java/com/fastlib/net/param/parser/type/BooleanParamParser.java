package com.fastlib.net.param.parser.type;

import com.fastlib.net.param.parser.SingleParamParser;

/**
 * Created by sgfb on 2019\12\19.
 */
public class BooleanParamParser extends SingleParamParser<Boolean> {
    @Override
    protected String parseParam(Boolean param) {
        return Boolean.toString(param);
    }
}
