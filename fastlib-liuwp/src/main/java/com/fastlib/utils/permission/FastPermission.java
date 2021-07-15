package com.fastlib.utils.permission;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/***
 * Created by liuwp on 2020/8/5
 *
 * 使用FastPermission进行权限申请，说明：
 *
 *  ①问世背景：流行的几个第三方权限申请库各有优缺点而且修改起来不方便.
 *  ②设计原则：代码解耦、链式调用.
 *  ③使用范围：只要有Context的地方就可以使用 ,如果添加了当前版本没有的权限，会自动移除，不会参与实际的权限申请。
 *  ②使用方式：链式调用。
 **/
public class FastPermission{

    private String[] mPermissions;
    private String mFailureHint;
    private String mRationaleHint;

    private FastPermission(Builder builder){
        this.mPermissions = builder.mPermissions;
        this.mFailureHint = builder.mFailureHint;
        this.mRationaleHint = builder.mRationaleHint;
    }

    private void request(Context context,OnPermissionCallback permissionCallback){
        if(mPermissions==null || mPermissions.length==0){
            Toast.makeText(context,"实际参与权限申请的权限个数为零",Toast.LENGTH_SHORT).show();
        }else{
            PermissionActivity.start(context,mPermissions,mFailureHint,mRationaleHint,permissionCallback);
        }
    }

    public static Builder with(Context context){
        return new Builder(context);
    }

    public static class Builder{
        private Context context;
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
        public Builder failureHint(@Nullable String hint){
            mFailureHint = hint;
            return this;
        }

        //基本原理提示,不设置则使用默认提示.
        public Builder rationaleHint(@Nullable String hint){
            mRationaleHint = hint;
            return this;
        }

        //添加权限
        public Builder permissions(@NonNull @PermissionDef String... permissions){
            mPermissions = PermissionUtil.getRealRequestPermissions(permissions);
            return this;
        }

        //申请权限
        public void request(OnPermissionCallback permissionCallback){
            build().request(context,permissionCallback);
        }
    }
}
