package com.fastlib.utils.permission;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuwp on 2020/8/5
 * 权限常量.
 * 系统权限文档:https://developer.android.google.cn/guide/topics/security/permissions.html
 */
public class Permission{

    ////////////////////////////////////////////////
    // 危险权限
    //////////////////////////////////////

    public static final String READ_CALENDAR = "android.permission.READ_CALENDAR";
    public static final String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";

    public static final String CAMERA = "android.permission.CAMERA";

    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";

    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    /**
     * Android 10(Q 29)开始后台定位权限需单独获取，获取前台定位权限成功后才能去获取后台定位权限，直接获取后台定位权限会失败。
     */
    public static final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";

    public static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";

    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public static final String CALL_PHONE = "android.permission.CALL_PHONE";
    public static final String USE_SIP = "android.permission.USE_SIP";
    public static final String READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS";
    public static final String ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS";
    public static final String ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";

    public static final String READ_CALL_LOG = "android.permission.READ_CALL_LOG";
    public static final String WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
    public static final String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";

    public static final String BODY_SENSORS = "android.permission.BODY_SENSORS";

    public static final String ACTIVITY_RECOGNITION = "android.permission.ACTIVITY_RECOGNITION";

    public static final String SEND_SMS = "android.permission.SEND_SMS";
    public static final String RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    public static final String READ_SMS = "android.permission.READ_SMS";
    public static final String RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";
    public static final String RECEIVE_MMS = "android.permission.RECEIVE_MMS";

    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    /**
     * 在Android 12(S 31) 之前，蓝牙权限在AndroidManifest.xml文件直接添加；
     * 从Android 12(S 31)开始，过去的蓝牙权限被拆分成了3个新的权限，并且全都是运行时权限：
     *  *  BLUETOOTH_SCAN 用于使用蓝牙扫描附件其他的蓝牙设备
     *  *  BLUETOOTH_ADVERTISE 用于允许当前的设备被其他的蓝牙设备所发现
     *  *  BLUETOOTH_CONNECT 用于连接之前已经配对过的蓝牙设备
     *
     *  <!-- Request legacy Bluetooth permissions on older devices. -->
     *  <uses-permission android:name="android.permission.BLUETOOTH"
     *      android:maxSdkVersion="30" />
     *  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"
     *      android:maxSdkVersion="30" />
     *  <!-- Request new Bluetooth permissions on android 12,12+ devices. -->
     *  <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
     *  <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
     *  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
     */
    public static final String BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";
    public static final String BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE";
    public static final String BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";

    ////////////////////////////////////////////////
    // 特殊权限
    //////////////////////////////////////

    public static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";

    public static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";

    /**
     * 从Android 10(Q 29)开始，Android系统默认启用Scoped Storage但不强制；
     * 从android 11(R 30)开始，强制启用Scoped Storage，所有App都不再拥有对SD卡进行全局读写的权限；
     * 设置android:requestLegacyExternalStorage="true"，
     * 则android 10及以下通过动态申请相关权限进行SD卡全局读写，从android 11开始对SD卡进行全局读写需申请MANAGE_EXTERNAL_STORAGE这个特殊权限。
     */
    public static final String MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE";

    /**
     * Android 8.0(O 26)系统开始对安装未知来源的应用程序进行了限制。
     * 在8.0系统之前，只要用户在手机设置中开启了“允许安装未知来源的应用”这个选项，那么就可以在这台手机上随意安装任意的APK。
     * 而从8.0系统开始，每个应用程序如果想要去跳转安装一个APK，都需要单独让用户去同意一遍“允许安装未知来源的的应用”这个选项才行。
     */
    public static final String REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES";

    ////////////////////////////////////////////////
    // 将权限转换为文本
    //////////////////////////////////////

    public static List<String> transformText(Context context,String... permissions){
        return transformText(context,Arrays.asList(permissions));
    }

    public static List<String> transformText(Context context,String[]... groups){
        List<String> permissionList = new ArrayList<>();
        for(String[] group: groups){
            permissionList.addAll(Arrays.asList(group));
        }
        return transformText(context,permissionList);
    }

    public static List<String> transformText(Context context,List<String> permissions){
        List<String> textList = new ArrayList<>();
        for(String permission: permissions){
            switch(permission){
                case Permission.READ_CALENDAR:
                case Permission.WRITE_CALENDAR:{
                    String message = "日历";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.CAMERA:{
                    String message = "相机";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.GET_ACCOUNTS:
                case Permission.READ_CONTACTS:
                case Permission.WRITE_CONTACTS:{
                    String message = "手机账号/通讯录";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.ACCESS_FINE_LOCATION:
                case Permission.ACCESS_COARSE_LOCATION:{
                    String message;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        message = "前台获取位置信息";
                    else
                        message = "位置信息";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.ACCESS_BACKGROUND_LOCATION:{
                    String message;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        message = "后台获取位置信息";
                    else
                        message = "";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.RECORD_AUDIO:{
                    String message = "麦克风";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.READ_PHONE_STATE:
                case Permission.CALL_PHONE:
                case Permission.ADD_VOICEMAIL:
                case Permission.USE_SIP:
                case Permission.READ_PHONE_NUMBERS:
                case Permission.ANSWER_PHONE_CALLS:{
                    String message = "电话";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.READ_CALL_LOG:
                case Permission.WRITE_CALL_LOG:
                case Permission.PROCESS_OUTGOING_CALLS:{
                    String message;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        message = "通话记录";
                    else
                        message = "电话";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.BODY_SENSORS:{
                    String message = "身体传感器";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.ACTIVITY_RECOGNITION:{
                    String message = "健身运动";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.SEND_SMS:
                case Permission.RECEIVE_SMS:
                case Permission.READ_SMS:
                case Permission.RECEIVE_WAP_PUSH:
                case Permission.RECEIVE_MMS:{
                    String message = "短信";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.READ_EXTERNAL_STORAGE:
                case Permission.WRITE_EXTERNAL_STORAGE:{
                    String message = "手机存储";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.SYSTEM_ALERT_WINDOW:{
                    String message = "显示在其他应用上层";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.WRITE_SETTINGS:{
                    String message = "修改系统设置";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.MANAGE_EXTERNAL_STORAGE:{
                    String message = "所有文件访问";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.REQUEST_INSTALL_PACKAGES:{
                    String message = "安装应用";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.BLUETOOTH_SCAN:
                case Permission.BLUETOOTH_ADVERTISE:
                case Permission.BLUETOOTH_CONNECT:{
                    String message;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        message = "蓝牙";
                    else
                        message = "";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
            }
        }
        return textList;
    }
}