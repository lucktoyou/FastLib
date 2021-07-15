package com.fastlib.net.param.interpreter.type;

import android.text.TextUtils;

import androidx.core.util.Pair;

import com.fastlib.net.param.RequestParam;
import com.fastlib.net.param.interpreter.SingleInterpreter;
import com.fastlib.utils.FastLog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 2019\12\24.
 * Modified by liuwp on 2020\9\24.
 * Url参数解释器
 */
public class UrlParamInterpreter extends SingleInterpreter {

    @Override
    protected InputStream interpreterAdapter(RequestParam param) {
        cleanIllegalParam(param);
        StringBuilder sb = new StringBuilder();
        sb.append('?');
        for (Map.Entry<String, List<String>> entry : param.getSurfaceParam().entrySet()) {
            for (String value : entry.getValue()) {
                try {
                    sb.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(value, "UTF-8")).append('&');
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    //清除违规参数
    private void cleanIllegalParam(RequestParam param) {
        List<Pair<String, Object>> bottomParam = param.getBottomParam();
        for (Pair<String, Object> pair : bottomParam) {
            String errorParamStr = "移除违规请求参数(BottomParam)：";
            if (TextUtils.isEmpty(pair.first)) errorParamStr += pair.second;
            else errorParamStr += pair.first + "=" + pair.second;
            FastLog.e(errorParamStr);
        }
        bottomParam.clear();
    }
}
