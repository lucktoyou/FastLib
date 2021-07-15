package com.fastlib.net.param.interpreter;

import androidx.annotation.NonNull;

import com.fastlib.net.param.RequestParam;
import com.fastlib.net.upload.ValuePosition;

import java.io.InputStream;
import java.util.List;

/**
 * Created by sgfb on 2019\12\24.
 * 参数解析器.解析{@link RequestParam}为Http请求时支持的参数（InputStream列表）
 */
public interface ParamInterpreter {

    /**
     * 参数解析
     * @param param             网络请求原始参数容器
     * @param valuePositions    可选的输出字段，存储每个字段对应值得长度，影响上传监听器
     * @return  解析为多个标准输入流
     */
    List<InputStream> interpreter(RequestParam param, @NonNull List<ValuePosition> valuePositions);
}
