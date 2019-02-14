package com.fastlib.utils.permission;


public interface PermissionCallback {

    void onPermissionSuccess();

    void onPermissionFailure(String hint);
}
