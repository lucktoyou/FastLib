package com.fastlib.utils.permission;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/***
 * Created by liuwp on 2020/8/5
 * 用于具体的权限申请.
 **/
public class PermissionActivity extends AppCompatActivity{
    private static final String ARG_PERMISSIONS = "permissions";
    private static final String ARG_FAILURE_HINT = "failureHint";
    private static final String ARG_RATIONALE_HINT = "rationaleHint";

    private static OnPermissionCallback mPermissionCallback;

    private PermissionActivity mOwner;
    private AlertDialog mAlertDialog;
    private String[] mPermissions;
    private String mFailureHint;
    private String mRationaleHint;
    private boolean mFirstRequestBackgroundLocationPermission = true;//是否是第一次进行后台定位权限申请


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setWindowAttributes();
        init();
        startPermissionsRequest();
    }

    //one pixel activity attribute
    private void setWindowAttributes(){
        Window window = getWindow();
        window.setGravity(Gravity.START|Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
    }

    private void init(){
        mOwner = this;
        mPermissions = getIntent().getStringArrayExtra(ARG_PERMISSIONS);
        mFailureHint = getIntent().getStringExtra(ARG_FAILURE_HINT);
        mRationaleHint = getIntent().getStringExtra(ARG_RATIONALE_HINT);
    }

    private void startPermissionsRequest(){
        PermissionUtil.checkPermissionsWhetherRegisteredInManifest(mOwner,mPermissions);
        if((Build.VERSION.SDK_INT<Build.VERSION_CODES.M) || PermissionUtil.checkPermissionsGranted(mOwner,mPermissions)){
            if(mPermissionCallback!=null){
                mPermissionCallback.onPermissionSuccess();
                finish();
            }
        }else{
            final String[] unauthorizedPermissions = PermissionUtil.getUnauthorizedPermissionList(mOwner,mPermissions);
            if(PermissionUtil.shouldShowRequestPermissionRationale(mOwner,mPermissions)){
                //如果用户之前拒绝过此权限，再提示一次准备授权相关权限
                if(TextUtils.isEmpty(mRationaleHint))
                    mRationaleHint = String.format("%s申请%s权限",PermissionUtil.getApplicationName(mOwner),
                            PermissionUtil.getPermissionToTransformText(mOwner,PermissionUtil.getUnauthorizedPermissionList(mOwner,mPermissions)));
                mAlertDialog = new AlertDialog.Builder(mOwner)
                        .setTitle("提示")
                        .setMessage(mRationaleHint)
                        .setCancelable(false)
                        .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                finish();
                            }
                        })
                        .setPositiveButton("下一步",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                if(checkNeedRequestBackgroundLocationPermission(Arrays.asList(unauthorizedPermissions))){
                                    mFirstRequestBackgroundLocationPermission = false;
                                }
                                ActivityCompat.requestPermissions(mOwner,unauthorizedPermissions,PermissionUtil.PERMISSION_REQUEST_CODE);
                            }
                        }).create();
                if(mAlertDialog!=null)
                    mAlertDialog.show();
            }else{
                ActivityCompat.requestPermissions(mOwner,unauthorizedPermissions,PermissionUtil.PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==PermissionUtil.PERMISSION_REQUEST_CODE){
            if(PermissionUtil.checkPermissionsGranted(grantResults)){
                if(mPermissionCallback!=null){
                    mPermissionCallback.onPermissionSuccess();
                    finish();
                }
            }else{
                if(TextUtils.isEmpty(mFailureHint))
                    mFailureHint = String.format("%s申请%s权限失败",PermissionUtil.getApplicationName(mOwner),
                            PermissionUtil.getPermissionToTransformText(mOwner,PermissionUtil.getUnauthorizedPermissionList(mOwner,permissions)));
                if(PermissionUtil.shouldShowRequestPermissionRationale(mOwner,permissions)){
                    if(checkNeedRequestBackgroundLocationPermission(Arrays.asList(permissions))){
                        mFirstRequestBackgroundLocationPermission = false;
                        mPermissions = PermissionUtil.getUnauthorizedPermissionList(mOwner,permissions);
                        ActivityCompat.requestPermissions(mOwner,mPermissions,PermissionUtil.PERMISSION_REQUEST_CODE);
                        return;
                    }
                    if(mPermissionCallback!=null){
                        mPermissionCallback.onPermissionFailure(mFailureHint);
                        finish();
                    }
                }else{
                    mAlertDialog = new AlertDialog.Builder(mOwner)
                            .setTitle("提示")
                            .setMessage(mFailureHint)
                            .setCancelable(false)
                            .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog,int which){
                                    finish();
                                }
                            })
                            .setPositiveButton("手动开启",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog,int which){
                                    PermissionUtil.toApplicationSetting(mOwner);
                                    finish();
                                }
                            }).create();
                    if(mAlertDialog!=null)
                        mAlertDialog.show();
                }
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mAlertDialog!=null){
            mAlertDialog = null;
        }
    }

    //检测是否需要获取后台定位权限。 Android 10(Q 29)开始后台定位权限需单独获取。获取前台定位权限成功后，才能去获取后台定位权限；直接获取后台定位权限会失败。
    private boolean checkNeedRequestBackgroundLocationPermission(@NonNull List<String> permissions){
        return Build.VERSION.SDK_INT >= 29 && permissions.contains(Permission.ACCESS_BACKGROUND_LOCATION) && !PermissionUtil.checkPermissionGranted(this,Permission.ACCESS_BACKGROUND_LOCATION) &&
                (PermissionUtil.checkPermissionGranted(this,Permission.ACCESS_FINE_LOCATION) || PermissionUtil.checkPermissionGranted(this,Permission.ACCESS_COARSE_LOCATION)) &&
                mFirstRequestBackgroundLocationPermission;
    }

    public static void start(Context context,String[] permissions,String failureHint,String rationaleHint,OnPermissionCallback permissionCallback){
        Intent intent = new Intent(context,PermissionActivity.class);
        intent.putExtra(ARG_PERMISSIONS,permissions);
        intent.putExtra(ARG_FAILURE_HINT,failureHint);
        intent.putExtra(ARG_RATIONALE_HINT,rationaleHint);
        context.startActivity(intent);

        mPermissionCallback = permissionCallback;
    }
}
