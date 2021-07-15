package com.fastlib.net.param.parser.type;

import com.fastlib.net.param.parser.SingleParamParser;

/**
 * Created by sgfb on 2019\12\18.
 */
public class IntParamParser extends SingleParamParser<Integer> {

    @Override
    protected String parseParam(Integer param){
        return Integer.toString(param);
    }
}
