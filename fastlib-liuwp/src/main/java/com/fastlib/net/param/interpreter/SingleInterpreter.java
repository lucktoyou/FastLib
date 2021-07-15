package com.fastlib.net.param.interpreter;

import androidx.annotation.NonNull;

import com.fastlib.net.param.RequestParam;
import com.fastlib.net.upload.ValuePosition;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgfb on 2019\12\24.
 */
public abstract class SingleInterpreter implements ParamInterpreter {

    protected abstract InputStream interpreterAdapter(RequestParam param);

    @Override
    public List<InputStream> interpreter(RequestParam param, @NonNull List<ValuePosition> valuePositions) {
        List<InputStream> list=new ArrayList<>();
        list.add(interpreterAdapter(param));
        return list;
    }
}
