package com.fastlib.utils.zipimage;

import java.io.File;

/**
 * Modified by liuwp 2020/11/23.
 * 压缩回调。
 */
public interface OnCompressListener {

    /**
     * 压缩启动时触发
     */
    void onStart();

    /**
     * 压缩成功返回时触发
     * @param file 符合压缩条件的已压缩文件或不符合压缩条件的源文件
     */
    void onSuccess(File file);

    /**
     * 压缩失败时触发
     */
    void onError(Throwable e);
}
