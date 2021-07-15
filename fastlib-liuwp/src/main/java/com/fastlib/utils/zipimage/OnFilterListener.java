package com.fastlib.utils.zipimage;

/**
 * Modified by liuwp 2020/11/23.
 * 过滤条件.
 */
public interface OnFilterListener {

    /**
     * @param source 传入源文件路径,路径：①file.getAbsolutePath() ②uri.getPath().
     * @return true允许压缩源文件 false不允许
     */
    boolean allowFileCompress(String source);
}
