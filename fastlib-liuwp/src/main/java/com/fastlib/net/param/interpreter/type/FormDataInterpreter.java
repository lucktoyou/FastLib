package com.fastlib.net.param.interpreter.type;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.fastlib.net.param.RequestParam;
import com.fastlib.net.param.interpreter.ParamInterpreter;
import com.fastlib.net.upload.ValuePosition;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 2019\12\24.
 * 将请求参数解释为符合FormData类型的输出
 */
public class FormDataInterpreter implements ParamInterpreter {
    public static final String BOUNDARY = "----AndroidFormBoundary" + Long.toHexString(System.currentTimeMillis());
    private final String CRLF = "\r\n";
    private final String START = "--" + BOUNDARY + CRLF;
    private final String END = "--" + BOUNDARY + "--" + CRLF;
    long mPositionMark =0;
    List<InputStream> mList =new ArrayList<>();
    List<ValuePosition> mFilePositions;

    @Override
    public List<InputStream> interpreter(RequestParam param, @NonNull List<ValuePosition> valuePositions) {
        mFilePositions=valuePositions;
        Map<String, List<String>> surfaceParam = param.getSurfaceParam();

        StringBuilder strSb = new StringBuilder();
        //字符串参数
        for (Map.Entry<String, List<String>> entry : surfaceParam.entrySet()) {
            for (String value : entry.getValue()) {
                strSb.append(START)
                        .append("Content-Disposition: form-data; name=").append('"').append(entry.getKey()).append('"').append(CRLF).append(CRLF)
                        .append(value).append(CRLF);
            }
        }
        addInputStream(null,new ByteArrayInputStream(strSb.toString().getBytes()));

        //文件参数
        StringBuilder fileSb=new StringBuilder();
        for(Pair<String,Object> pair:param.getBottomParam()){
            if(pair.second instanceof File){
                File file= (File) pair.second;
                fileSb.setLength(0);
                fileSb.append(START)
                        .append("Content-Disposition: form-data; name=").append('"').append(pair.first).append('"').append("; ")
                        .append("filename=").append('"').append(file.getName()).append('"').append(CRLF)
                        .append("Content-type: ").append(URLConnection.guessContentTypeFromName(file.getName())).append(CRLF).append(CRLF);
                addInputStream(pair.first,new ByteArrayInputStream(fileSb.toString().getBytes()));
                try {
                    addInputStream(pair.first,new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                addInputStream(null,new ByteArrayInputStream(CRLF.getBytes()));
            }
        }
        addInputStream(null,new ByteArrayInputStream(END.getBytes()));
        return mList;
    }

    private void addInputStream(String key,InputStream in) {
        mList.add(in);
        try{
            long length=in.available();
            if(in instanceof FileInputStream)
                mFilePositions.add(new ValuePosition(mPositionMark,length,key));
            mPositionMark+=length;
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
