package com.fastlib.utils.permission;

/***
 * Created by liuwp on 2020/8/5
 * 权限申请结果回调.
 **/
public interface OnPermissionCallback{

    void onPermissionSuccess();

    void onPermissionFailure(String hint);

    //void onPermissionGranted(List<String> grantedPermissions);

    //void onPermissionDenied(List<String> deniedPermissions);

    //void onPermissionAlwaysDenied(List<String> alwaysDeniedPermissions);
}
