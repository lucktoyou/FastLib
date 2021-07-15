package com.fastlib.net.upload;

/**
 * Created by sgfb on 2020\04\03.
 * 网络请求在上传时回调
 */
public interface UploadingListener{

    /**
     * 上传回调
     * @param key       对应参数名
     * @param wrote     已写出量（已写到往服务器传输的流中但是不一定成功传到服务器，最终需要在业务层自行确定）
     * @param count     总量
     */
    void uploading(String key, long wrote, long count);
}
