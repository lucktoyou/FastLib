package com.fastlib.utils.permission;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuwp on 2020/8/5
 * 权限常量.
 * 系统权限(含危险权限和权限组表):https://developer.android.google.cn/guide/topics/security/permissions.html
 * 参考自 https://github.com/yanzhenjie/AndPermission/blob/master/permission/src/main/java/com/yanzhenjie/permission/runtime/Permission.java
 */
public class Permission{

    public static final String READ_CALENDAR = "android.permission.READ_CALENDAR";
    public static final String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";

    public static final String CAMERA = "android.permission.CAMERA";

    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";

    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
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

    public static final class Group{

        public static final String[] CALENDAR = new String[]{Permission.READ_CALENDAR,Permission.WRITE_CALENDAR};

        public static final String[] CAMERA = new String[]{Permission.CAMERA};

        public static final String[] CONTACTS = new String[]{Permission.READ_CONTACTS,Permission.WRITE_CONTACTS,Permission.GET_ACCOUNTS};

        //Android 10(Q 29)开始后台定位权限需单独获取。
        //获取前台定位权限成功后，才能去获取后台定位权限；直接获取后台定位权限会失败。
        //系统SDK版本小于29，无后台定位权限，FastPermission这里有做适配工作，允许你直接添加location permission group，在进行权限请求前会帮你移除不参与实际申请的权限。
        public static final String[] LOCATION = new String[]{Permission.ACCESS_FINE_LOCATION,Permission.ACCESS_COARSE_LOCATION,Permission.ACCESS_BACKGROUND_LOCATION};

        public static final String[] MICROPHONE = new String[]{Permission.RECORD_AUDIO};

        public static final String[] PHONE = new String[]{Permission.READ_PHONE_STATE,Permission.CALL_PHONE,Permission.USE_SIP,Permission.READ_PHONE_NUMBERS,Permission.ANSWER_PHONE_CALLS,Permission.ADD_VOICEMAIL};

        public static final String[] CALL_LOG = new String[]{Permission.READ_CALL_LOG,Permission.WRITE_CALL_LOG,Permission.PROCESS_OUTGOING_CALLS};

        public static final String[] SENSORS = new String[]{Permission.BODY_SENSORS};

        public static final String[] ACTIVITY_RECOGNITION = new String[]{Permission.ACTIVITY_RECOGNITION};

        public static final String[] SMS = new String[]{Permission.SEND_SMS,Permission.RECEIVE_SMS,Permission.READ_SMS,Permission.RECEIVE_WAP_PUSH,Permission.RECEIVE_MMS};

        public static final String[] STORAGE = new String[]{Permission.READ_EXTERNAL_STORAGE,Permission.WRITE_EXTERNAL_STORAGE};
    }

    /**
     * Turn permissions into text.
     */
    public static List<String> transformText(Context context,String... permissions){
        return transformText(context,Arrays.asList(permissions));
    }

    /**
     * Turn permissions into text.
     */
    public static List<String> transformText(Context context,String[]... groups){
        List<String> permissionList = new ArrayList<>();
        for(String[] group: groups){
            permissionList.addAll(Arrays.asList(group));
        }
        return transformText(context,permissionList);
    }

    /**
     * Turn permissions into text.
     */
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
                    if(Build.VERSION.SDK_INT >= 29)
                        message = "前台定位";
                    else
                        message = "定位";
                    if(!textList.contains(message)){
                        textList.add(message);
                    }
                    break;
                }
                case Permission.ACCESS_BACKGROUND_LOCATION:{
                    String message;
                    if(Build.VERSION.SDK_INT >= 29)
                        message = "后台定位";
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
                    if(Build.VERSION.SDK_INT >= 29)
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
                    String message = "存储空间";
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