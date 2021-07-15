package com.fastlib.net.upload;

/**
 * Created by sgfb on 2020\04\04.
 * 当网络请求参数被转换成InputStream时,对应的参数值在流中的位置
 */
public class ValuePosition {
    public long start;
    public long length;
    public String key;

    public ValuePosition(long start, long length, String key) {
        this.start = start;
        this.length = length;
        this.key = key;
    }
}
