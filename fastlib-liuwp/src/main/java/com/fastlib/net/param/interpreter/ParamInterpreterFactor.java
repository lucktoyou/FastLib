package com.fastlib.net.param.interpreter;

import androidx.annotation.StringDef;

import com.fastlib.net.core.HeaderDefinition;
import com.fastlib.net.param.interpreter.type.FormDataInterpreter;
import com.fastlib.net.param.interpreter.type.FormUrlEncodedInterpreter;
import com.fastlib.net.param.interpreter.type.JsonInterpreter;
import com.fastlib.net.param.interpreter.type.UrlParamInterpreter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sgfb on 2019\12\24.
 * 参数解析器工厂
 */
public class ParamInterpreterFactor{
    public static final String BODY_URL_PARAM="urlParam";
    public static final String BODY_FORM_URLENCODED=HeaderDefinition.VALUE_CONTENT_TYPE_X_WWW_FORM_URLENCODED;
    public static final String BODY_FORM_DATA=HeaderDefinition.VALUE_CONTENT_TYPE_MULTIPART_FORM_DATA;
    public static final String BODY_RAW_JSON=HeaderDefinition.VALUE_CONTENT_TYPE_JSON;

    @StringDef({
            BODY_URL_PARAM,
            BODY_FORM_URLENCODED,
            BODY_FORM_DATA,
            BODY_RAW_JSON
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ParamInterpreterType{}

    private ParamInterpreterFactor(){}

    public static ParamInterpreter getInterpreter(@ParamInterpreterType String type){
        switch (type){
            case BODY_URL_PARAM:return new UrlParamInterpreter();
            case BODY_FORM_URLENCODED:return new FormUrlEncodedInterpreter();
            case BODY_FORM_DATA:return new FormDataInterpreter();
            case BODY_RAW_JSON:return new JsonInterpreter();
        }
        throw new UnsupportedOperationException("不支持的类型:"+type);
    }
}
