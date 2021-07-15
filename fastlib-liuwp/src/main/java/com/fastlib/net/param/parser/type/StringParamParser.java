package com.fastlib.net.param.parser.type;

import com.fastlib.net.param.parser.SingleParamParser;

/**
 * Created by sgfb on 2019\12\25.
 */
public class StringParamParser extends SingleParamParser<String> {

    @Override
    protected String parseParam(String param) {
        return param;
    }
}
