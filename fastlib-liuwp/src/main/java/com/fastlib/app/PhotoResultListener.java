package com.fastlib.app;

/**
 * Created by sgfb on 17/2/21.
 * 请求相机或相册时图像回调接口
 */
public interface PhotoResultListener{
    void onPhotoResult(String path);
}