package com.fastlib.utils.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.fastlib.app.FastActivity;
import com.fastlib.utils.AppUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuwp 2017/12/21
 *
 * Android 6.0以上运行时权限请求辅助类
 */
public class RunTimePermissionUtil {
    
    public static final int PERMISSION_REQUEST_CODE = 1000;

    /**
     * 读写文件权限
     * @param activity
     * @param callback
     */
    public static void onStorage(final Activity activity, final PermissionCallback callback){
        if (activity != null && !activity.isFinishing() && activity instanceof FastActivity) {
            ((FastActivity) activity).performCodeWithPermission(activity, AppUtil.getAppName(activity)+"申请获取读写文件权限", new PermissionCallback() {
                @Override
                public void onPermissionSuccess() {
                    if (null != callback) {
                        callback.onPermissionSuccess();
                    }
                }

                @Override
                public void onPermissionFailure(String hint) {
                    if (null != callback) {
                        callback.onPermissionFailure(hint);
                    }
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * 手机通讯录权限
     * @param activity
     * @param callback
     */
    public static void onReadPhones(final Activity activity, final PermissionCallback callback){
        if (activity != null && !activity.isFinishing() && activity instanceof FastActivity) {
            ((FastActivity) activity).performCodeWithPermission(activity, AppUtil.getAppName(activity)+"申请读取您的通讯录", new PermissionCallback() {
                @Override
                public void onPermissionSuccess() {
                    if (null != callback) {
                        callback.onPermissionSuccess();
                    }
                }

                @Override
                public void onPermissionFailure(String hint) {
                    if (null != callback) {
                        callback.onPermissionFailure(hint);
                    }
                }
            }, Manifest.permission.READ_CONTACTS);
        }
    }

    /**
     * 相机权限
     * @param activity
     * @param callback
     */
    public static void onCamera(final Activity activity, final PermissionCallback callback){
        if (activity != null && !activity.isFinishing() && activity instanceof FastActivity) {
            ((FastActivity) activity).performCodeWithPermission(activity, AppUtil.getAppName(activity)+"申请获取相机相关权限", new PermissionCallback() {
                @Override
                public void onPermissionSuccess() {
                    if (null != callback) {
                        callback.onPermissionSuccess();
                    }
                }

                @Override
                public void onPermissionFailure(String hint) {
                    if (null != callback) {
                        callback.onPermissionFailure(hint);
                    }
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA);
        }
    }

    /**
     * 初始化获取手机号码/设备ID
     * @param activity
     * @param callback
     */
    public static void onPhoneNum(final Activity activity, final PermissionCallback callback) {
        if (activity != null && !activity.isFinishing() && activity instanceof FastActivity) {
            ((FastActivity) activity).performCodeWithPermission(activity, AppUtil.getAppName(activity)+"申请获取手机号码权限", new PermissionCallback() {
                @Override
                public void onPermissionSuccess() {
                    if (null != callback) {
                        callback.onPermissionSuccess();
                    }
                }

                @Override
                public void onPermissionFailure(String hint) {
                    if (null != callback) {
                        callback.onPermissionFailure(hint);
                    }
                }
            }, Manifest.permission.READ_PHONE_STATE);
        }
    }

    /**
     * 拨打电话
     * @param activity
     * @param phoneNum
     */
    public static void onCall(final Activity activity, final String phoneNum) {
        if (activity != null && !activity.isFinishing() && activity instanceof FastActivity && !TextUtils.isEmpty(phoneNum)){
            ((FastActivity) activity).performCodeWithPermission(activity, AppUtil.getAppName(activity)+"申请获取拨打电话权限", new PermissionCallback() {
                @Override
                public void onPermissionSuccess() {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("tel:" + phoneNum));
                    activity.startActivity(intent);
                }

                @Override
                public void onPermissionFailure(String hint) {
                    Toast.makeText(activity, hint, Toast.LENGTH_SHORT).show();
                }
            }, Manifest.permission.CALL_PHONE);
        }
    }

    /**
     * 定位
     * @param activity
     * @param callback
     */
    public static void onLocation(final Activity activity, final PermissionCallback callback) {
        if (activity != null && !activity.isFinishing() && activity instanceof FastActivity) {
            ((FastActivity) activity).performCodeWithPermission(activity, AppUtil.getAppName(activity)+"申请获取定位权限", new PermissionCallback() {
                @Override
                public void onPermissionSuccess() {
                    if (null != callback){
                        callback.onPermissionSuccess();
                    }
                }

                @Override
                public void onPermissionFailure(String hint) {
                    if (null != callback){
                        callback.onPermissionFailure(hint);
                    }
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    public static boolean checkPermissionGranted(Activity activity, String[] permissions){
        boolean flag = true;
        for(String p:permissions){
            if(ActivityCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED){
                flag = false;
                break;
            }
        }
        return flag;
    }

    public static void requestPermission(final Activity activity, final String permissionDes, final int requestCode, final String[] permissions){
        //过滤出没有授权的权限，重新授权
        List<String>  permissionList = new ArrayList<String>();
        for (String p : permissions) {
            if(ActivityCompat.checkSelfPermission(activity,p) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(p);
            }
        }
        final String[] unauthorizedPermissions = permissionList.toArray(new String[permissionList.size()]);

        if(shouldShowRequestPermissionRationale(activity, permissions)){
            //如果用户之前拒绝过此权限，再提示一次准备授权相关权限
            new AlertDialog.Builder(activity)
                    .setTitle("提示")
                    .setMessage(permissionDes)
                    .setPositiveButton("下一步", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, unauthorizedPermissions, requestCode);
                        }
                    }).show();
        }else{
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(activity, unauthorizedPermissions, requestCode);
        }
    }
    private static boolean shouldShowRequestPermissionRationale(Activity activity, String[] permissions){
        boolean flag = false;
        for(String p:permissions){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
