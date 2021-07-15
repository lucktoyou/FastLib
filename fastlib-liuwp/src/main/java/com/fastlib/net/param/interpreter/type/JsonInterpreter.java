package com.fastlib.net.param.interpreter.type;

import android.text.TextUtils;

import androidx.core.util.Pair;

import com.fastlib.net.param.RequestParam;
import com.fastlib.net.param.interpreter.SingleInterpreter;
import com.fastlib.utils.FastLog;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sgfb on 2019\12\24.
 * Modified by liuwp on 2020\9\24.
 * 对请求参数做符合application/json形式的输出
 */
public class JsonInterpreter extends SingleInterpreter {

    @Override
    protected InputStream interpreterAdapter(RequestParam param) {
        cleanIllegalParam(param);
        Gson gson = new Gson();
        for (Pair<String, Object> pair : param.getBottomParam()) {
            if ((pair.first == null) && !(pair.second instanceof File))
                return new ByteArrayInputStream(gson.toJson(pair.second).getBytes());
        }
        return new ByteArrayInputStream("".getBytes());
    }

    //清除违规参数
    private void cleanIllegalParam(RequestParam param) {
        Set<Map.Entry<String, List<String>>> surfaceParam = param.getSurfaceParam().entrySet();
        for (Map.Entry<String, List<String>> entry : surfaceParam) {
            for (String value : entry.getValue()) {
                String errorParamStr = "移除违规请求参数(SurfaceParam)：";
                if (TextUtils.isEmpty(entry.getKey())) errorParamStr += value;
                else errorParamStr += entry.getKey() + "=" + value;
                FastLog.e(errorParamStr);
            }
        }
        surfaceParam.clear();
        List<Pair<String, Object>> bottomParam = param.getBottomParam();
        for (Pair<String, Object> pair : bottomParam) {
            if (pair.first != null || pair.second instanceof File) {
                String errorParamStr = "移除违规请求参数(BottomParam)：";
                if (TextUtils.isEmpty(pair.first)) errorParamStr += pair.second;
                else errorParamStr += pair.first + "=" + pair.second;
                FastLog.e(errorParamStr);
                bottomParam.remove(pair);
            }
        }
    }
}
