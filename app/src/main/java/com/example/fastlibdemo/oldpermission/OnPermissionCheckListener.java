package com.example.fastlibdemo.oldpermission;


/**
 * Created by liuwp 2017/12/21.
 * Modified by liuwp 2020/3/25.
 * 权限检测回调。
 */
@Deprecated
public interface OnPermissionCheckListener {

    void onPermissionSuccess();

    void onPermissionFailure(String hint);
}
