package com.fastlib.net.param.parser.type;

import com.fastlib.net.param.parser.SingleParamParser;

/**
 * Created by sgfb on 2019\12\19.
 */
public class DoubleParamParser extends SingleParamParser<Double> {
    @Override
    protected String parseParam(Double param) {
        return Double.toString(param);
    }
}
