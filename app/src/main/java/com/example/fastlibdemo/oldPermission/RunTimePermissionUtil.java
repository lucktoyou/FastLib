package com.example.fastlibdemo.oldPermission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuwp 2017/12/21.
 * Modified by liuwp 2020/3/25.
 * <p>
 * Android 6.0以上运行时权限请求辅助类；默认AndroidManifest.xml文件已添加需申请权限.
 */
@Deprecated
public class RunTimePermissionUtil {

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static OnPermissionCheckListener mOnPermissionCheckListener;
    private static String mPermissionDescription;
    private static List<String> mAppPermissions;
    private static AlertDialog mAlertDialog;

    /**
     * 读写文件权限
     *
     * @param activity
     * @param callback
     */
    public static void onStorage(final Activity activity, final OnPermissionCheckListener callback) {
        if (activity != null && !activity.isFinishing()) {
            requestPermission(activity, getApplicationName(activity) + "申请获取读写文件权限", new OnPermissionCheckListener() {
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
            },Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * 相机权限
     *
     * @param activity
     * @param callback
     */
    public static void onCamera(final Activity activity, final OnPermissionCheckListener callback) {
        if (activity != null && !activity.isFinishing()) {
            requestPermission(activity, getApplicationName(activity) + "申请获取相机权限", new OnPermissionCheckListener() {
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
            },Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
        }
    }

    /**
     * 手机通讯录权限
     *
     * @param activity
     * @param callback
     */
    public static void onReadContacts(final Activity activity, final OnPermissionCheckListener callback) {
        if (activity != null && !activity.isFinishing()) {
            requestPermission(activity, getApplicationName(activity) + "申请读取您的通讯录", new OnPermissionCheckListener() {
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
     * 初始化获取手机号码/设备ID
     *
     * @param activity
     * @param callback
     */
    public static void onPhoneState(final Activity activity, final OnPermissionCheckListener callback) {
        if (activity != null && !activity.isFinishing()) {
            requestPermission(activity, getApplicationName(activity) + "申请获取手机状态权限", new OnPermissionCheckListener() {
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
     *
     * @param activity
     * @param phoneNum
     */
    public static void onCallPhone(final Activity activity, final String phoneNum) {
        if (activity != null && !activity.isFinishing()) {
            requestPermission(activity, getApplicationName(activity) + "申请获取拨打电话权限", new OnPermissionCheckListener() {
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
     *
     * @param activity
     * @param callback
     */
    public static void onLocation(final Activity activity, final OnPermissionCheckListener callback) {
        if (activity != null && !activity.isFinishing()) {
            requestPermission(activity, getApplicationName(activity) + "申请获取定位权限", new OnPermissionCheckListener() {
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
            }, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * 定位
     *
     * @param activity
     * @param callback
     */
    public static void onLocation2(final Activity activity, final OnPermissionCheckListener callback) {
        if (activity != null && !activity.isFinishing()) {
            requestPermission(activity, getApplicationName(activity) + "申请获取定位权限", new OnPermissionCheckListener() {
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
            }, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    public static void onHandlePermissionResult(@NonNull final Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (verifyPermissions(grantResults)) {
                if (mOnPermissionCheckListener != null) {
                    mOnPermissionCheckListener.onPermissionSuccess();
                    mOnPermissionCheckListener = null;
                }
            } else {
                if (shouldShowRequestPermissionRationale(activity, permissions)) {
                    if (mOnPermissionCheckListener != null) {
                        mOnPermissionCheckListener.onPermissionFailure(mPermissionDescription + "失败");
                        mOnPermissionCheckListener = null;
                    }
                } else {
                    mAlertDialog = new AlertDialog.Builder(activity)
                            .setTitle("提示")
                            .setMessage(mPermissionDescription + "失败,尝试手动开启")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    toApplicationSetting(activity);
                                }
                            })
                            .show();
                }
                mPermissionDescription = null;
            }
        }
    }

    /**
     * Android M运行时权限请求封装
     *
     * @param permissionDes 权限描述
     * @param runnable      请求权限回调
     * @param permissions   请求的权限（数组类型），直接从Manifest中读取相应的值，比如Manifest.permission.WRITE_CONTACTS
     */
    private static void requestPermission(@NonNull final Activity activity, @NonNull String permissionDes, OnPermissionCheckListener runnable, @NonNull String... permissions) {
        checkPermissionsWhetherRegisteredInManifest(activity, permissions);
        mOnPermissionCheckListener = runnable;
        mPermissionDescription = permissionDes;
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || checkPermissionGranted(activity, permissions)) {
            if (runnable != null) {
                runnable.onPermissionSuccess();
            }
        } else {
            //permission has not been granted.

            //过滤出没有授权的权限，重新授权
            List<String> permissionList = new ArrayList<String>();
            for (String p : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(p);
                }
            }
            final String[] mUnauthorizedPermissions = permissionList.toArray(new String[permissionList.size()]);

            if (shouldShowRequestPermissionRationale(activity, permissions)) {
                //如果用户之前拒绝过此权限，再提示一次准备授权相关权限
                mAlertDialog = new AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setMessage(permissionDes)
                        .setPositiveButton("下一步", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, mUnauthorizedPermissions, PERMISSION_REQUEST_CODE);
                            }
                        })
                        .show();
            } else {
                // Contact permissions have not been granted yet. Request them directly.
                ActivityCompat.requestPermissions(activity, mUnauthorizedPermissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private static boolean checkPermissionGranted(Activity activity, String[] permissions) {
        boolean flag = true;
        for (String p : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private static boolean shouldShowRequestPermissionRationale(Activity activity, String[] permissions) {
        boolean flag = false;
        for (String p : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
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

    //跳转到APP设置界面
    private static void toApplicationSetting(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    //获取应用程序名称
    private static String getApplicationName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    //检查权限是否在清单中注册
    private static void checkPermissionsWhetherRegisteredInManifest(Context context, String[] permissions) {
        if (mAppPermissions == null) {
            mAppPermissions = new ArrayList<>(getManifestPermissions(context));
        }
        if (permissions.length == 0) {
            throw new IllegalArgumentException("Please enter at least one permission.");
        }
        for (String target : permissions) {
            if (!mAppPermissions.contains(target)) {
                throw new IllegalStateException(String.format("The permission %1$s is not registered in manifest.xml", target));
            }
        }
    }

    //获取清单中的权限列表
    private static List<String> getManifestPermissions(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions == null || permissions.length == 0) {
                throw new IllegalStateException("You did not register any permissions in the manifest.xml.");
            }
            return Arrays.asList(permissions);
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError("Package name cannot be found.");
        }
    }
}
