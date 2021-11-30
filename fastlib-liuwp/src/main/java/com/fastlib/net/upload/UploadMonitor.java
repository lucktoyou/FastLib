package com.fastlib.net.upload;

/**
 * Created by sgfb on 2020\04\03.
 * Modified by liuwp on 2021\11\30.
 * 上传监视器.
 */
public interface UploadMonitor{

    /**
     * 上传回调
     *
     * @param key       对应网络请求参数名
     * @param wroteSize 已写出量（已写到往服务器传输的流中但是不一定成功传到服务器，最终需要在业务层自行确定）
     * @param rawSize   总量（单个上传文件大小）
     */
    void uploading(String key,long wroteSize,long rawSize);
}
