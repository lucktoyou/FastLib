package com.fastlib.utils.zipimage;


/**
 * Modified by liuwp 2020/11/23.
 * 重命名。
 */
public interface OnRenameListener {

  /**
   *@param source 传入源文件路径,路径：①file.getAbsolutePath() ②uri.getPath().
   * @return 压缩文件名,例：image.jpeg
   */
  String renameCompressFile(String source);
}
