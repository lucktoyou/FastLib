package com.fastlib.utils.permission;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fastlib.utils.N;


/***
 * Created by liuwp on 2020/8/5
 *
 * 使用FastPermission进行【危险权限单个多个、特殊权限单个】申请，说明：
 *
 *  ①问世背景：流行的几个第三方权限申请库各有优缺点而且修改起来不方便。
 *  ②设计原则：代码解耦、链式调用。
 *  ③适配范围：android api（4.4 - 12.0）
 *  ②使用方式：链式调用。
 **/
public class FastPermission{

    private String[] mPermissions;
    private final String mFailureHint;
    private final String mRationaleHint;

    private FastPermission(Builder builder){
        this.mPermissions = builder.mPermissions;
        this.mFailureHint = builder.mFailureHint;
        this.mRationaleHint = builder.mRationaleHint;
    }

    private void request(Context context,OnPermissionCallback permissionCallback){
        mPermissions = PermissionUtil.getRealRequestPermissions(mPermissions);
        if (mPermissions==null || mPermissions.length==0) {
            N.showToast(context,"滤除掉违规添加的权限后，没有多余的权限需要申请.");
        }else {
            PermissionHelper.getInstance().setPermissionCallback(permissionCallback);
            PermissionActivity.start(context,mPermissions,mFailureHint,mRationaleHint);
        }
    }


    public static Builder with(Context context){
        return new Builder(context);
    }

    public static class Builder{
        private final Context context;
        private String[] mPermissions;
        private String mFailureHint;
        private String mRationaleHint;


        Builder(Context context){
            this.context = context;
        }

        private FastPermission build(){
            return new FastPermission(this);
        }

        //失败提示,不设置则使用默认提示.
        public Builder setFailureHint(@Nullable String hint){
            mFailureHint = hint;
            return this;
        }

        //原因提示,不设置则使用默认提示.
        public Builder setRationaleHint(@Nullable String hint){
            mRationaleHint = hint;
            return this;
        }

        //添加权限
        public Builder permissions(@NonNull @PermissionDef String... permissions){
            mPermissions = permissions;
            return this;
        }

        //申请权限
        public void request(OnPermissionCallback permissionCallback){
            build().request(context,permissionCallback);
        }
    }
}
