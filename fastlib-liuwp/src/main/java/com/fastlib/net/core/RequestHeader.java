package com.fastlib.net.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuwp on 2020/4/28
 * 客户端网络请求时添加的Header，含固定Header和非固定Header.
 */
public class RequestHeader {
    private String method;
    private String url;
    private String protocol;
    private Map<String, List<String>> mHeader;

    public RequestHeader(String method, String url, String protocol, Map<String, List<String>> header) {
        this.method = method;
        this.url = url;
        this.protocol = protocol;
        this.mHeader = header;
        if(mHeader==null)
            mHeader=new HashMap<>();
    }

    public Map<String, List<String>> getHeaders() {
        return new HashMap<>(mHeader);
    }

    public List<String> getHeader(String key){
        return mHeader.get(key);
    }

    public String getHeaderFirst(String key){
        List<String> list=mHeader.get(key);
        if(list==null||list.isEmpty()) return null;
        return list.get(0);
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getProtocol() {
        return protocol;
    }
}
