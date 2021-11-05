package com.fastlib.utils.permission;

/**
 * Created by liuwp on 2021/10/19.
 */
public class PermissionHelper{
    private static PermissionHelper instance;
    private OnPermissionCallback mPermissionCallback;

    private PermissionHelper(){
    }

    public static synchronized PermissionHelper getInstance(){
        if(instance ==null){
            instance = new PermissionHelper();
        }
        return instance;
    }

    public void setPermissionCallback(OnPermissionCallback permissionCallback){
        mPermissionCallback = permissionCallback;
    }

    public OnPermissionCallback getPermissionCallback(){
        return mPermissionCallback;
    }
}
