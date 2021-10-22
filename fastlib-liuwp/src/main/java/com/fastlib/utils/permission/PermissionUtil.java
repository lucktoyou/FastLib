package com.fastlib.utils.permission;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuwp on 2020/8/5
 * 辅助工具
 */
public class PermissionUtil{
    public static final int REQUEST_CODE_PERMISSION = 101;
    public static final int REQUEST_CODE_INSTALL = 102;
    public static final int ACTION_MANAGE_OVERLAY_PERMISSION = 201;
    public static final int ACTION_WRITE_SETTINGS_PERMISSION = 202;
    public static final int ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION = 203;
    private static List<String> mAppPermissions;


    //过滤出实际需要去请求的权限
    public static String[] getRealRequestPermissions(@NonNull String[] permissions) {
        List<String> list = new ArrayList<>(Arrays.asList(permissions));
        List<String> realList = new ArrayList<>();
        for(int i = 0;i<list.size();i++){
            String p = list.get(i);
            //Android 10(Q 29)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && p.equals(Permission.ACCESS_BACKGROUND_LOCATION)) {
                continue;
            }
            //从android 11(R 30)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && p.equals(Permission.MANAGE_EXTERNAL_STORAGE)) {
                continue;
            }
            //Android 12(S 31)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && (p.equals(Permission.BLUETOOTH_ADVERTISE) || p.equals(Permission.BLUETOOTH_CONNECT) || p.equals(Permission.BLUETOOTH_SCAN))) {
               continue;
            }
            realList.add(p);
        }
        return realList.toArray(new String[0]);
    }

    //检查权限是否在清单中注册
    public static void checkPermissionsWhetherRegisteredInManifest(@NonNull Context context,@NonNull String[] permissions){
        if(mAppPermissions==null){
            mAppPermissions = new ArrayList<>(getManifestPermissions(context));
        }
        if(permissions.length==0){
            throw new IllegalArgumentException("Please enter at least one permission.");
        }
        for(String target: permissions){
            if(!mAppPermissions.contains(target)){
                throw new IllegalStateException(String.format("The permission %1$s is not registered in manifest.xml",target));
            }
        }
    }

    //获取清单中的权限列表
    public static List<String> getManifestPermissions(@NonNull Context context){
        try{
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),PackageManager.GET_PERMISSIONS);
            String[] permissions = packageInfo.requestedPermissions;
            if(permissions==null || permissions.length==0){
                throw new IllegalStateException("You did not register any permissions in the manifest.xml.");
            }
            return Arrays.asList(permissions);
        }catch(PackageManager.NameNotFoundException e){
            throw new AssertionError("Package name cannot be found.");
        }
    }

    //获取权限并转换成文本
    public static String getPermissionToTransformText(@NonNull Context context,@NonNull String[] permissions){
        if(permissions.length==0){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for(String name: Permission.transformText(context,permissions)){
            builder.append(name).append("、");
        }
        return String.valueOf(builder.deleteCharAt(builder.length()-1));
    }

    //获取应用程序名称
    public static String getApplicationName(@NonNull Context context){
        try{
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        }catch(PackageManager.NameNotFoundException e){
            return "";
        }
    }

    //跳转到APP设置界面
    public static void toApplicationSetting(@NonNull Activity activity){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",activity.getPackageName(),null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static void toApplicationSetting(@NonNull Fragment fragment){
        FragmentActivity activity = fragment.getActivity();
        if(activity!=null)
            toApplicationSetting(activity);
    }

    //检查权限是否授权
    public static boolean checkPermissionsGranted(@NonNull int[] grantResults){
        if(grantResults.length==0){
            return false;
        }
        for(int result: grantResults){
            if(result!=PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermissionsGranted(@NonNull Context context,@NonNull String[] permissions){
        if(permissions.length==0){
            return false;
        }
        for(String p: permissions){
            if(ContextCompat.checkSelfPermission(context,p)!=PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermissionGranted(@NonNull Context context,@NonNull String permission){
        return ContextCompat.checkSelfPermission(context,permission)==PackageManager.PERMISSION_GRANTED;
    }

    //过滤出没有授权的权限
    public static String[] getUnauthorizedPermissionList(@NonNull Context context,@NonNull String[] permissions){
        List<String> list = new ArrayList<>(Arrays.asList(permissions));
        if(list.size()>0){
            for(String p: permissions){
                if(ContextCompat.checkSelfPermission(context,p)==PackageManager.PERMISSION_GRANTED){
                    list.remove(p);
                }
            }
        }
        return list.toArray(new String[0]);//new String[0]指定返回数组的类型，0是为了节省空间，最终返回的String[]的长度list存储内容的长度决定.
    }

    //是否应该显示原理
    public static boolean shouldShowRequestPermissionRationale(@NonNull Activity activity,@NonNull String[] permissions){
        if(permissions.length==0){
            return false;
        }
        for(String p: permissions){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,p)){
                return true;
            }
        }
        return false;
    }

    public static boolean shouldShowRequestPermissionRationale(@NonNull Fragment fragment,@NonNull String[] permissions){
        if(permissions.length==0){
            return false;
        }
        for(String p: permissions){
            if(fragment.shouldShowRequestPermissionRationale(p)){
                return true;
            }
        }
        return false;
    }
}
