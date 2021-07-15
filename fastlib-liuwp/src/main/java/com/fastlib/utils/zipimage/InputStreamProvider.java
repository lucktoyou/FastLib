package com.fastlib.utils.zipimage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Modified by liuwp 2020/11/23.
 * 通过此接口获取输入流，以兼容文件、FileProvider方式获取到的图片。
 */
public interface InputStreamProvider {

    InputStream open() throws IOException;

    void close();

    String getPath();
}
