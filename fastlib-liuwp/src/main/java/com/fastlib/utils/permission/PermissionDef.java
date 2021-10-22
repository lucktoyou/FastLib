package com.fastlib.utils.permission;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by liuwp on 2020/8/6.
 * 设置允许添加的权限常量.
 */
@StringDef({
        Permission.READ_CALENDAR,
        Permission.WRITE_CALENDAR,
        Permission.CAMERA,
        Permission.READ_CONTACTS,
        Permission.WRITE_CONTACTS,
        Permission.GET_ACCOUNTS,
        Permission.ACCESS_FINE_LOCATION,
        Permission.ACCESS_COARSE_LOCATION,
        Permission.ACCESS_BACKGROUND_LOCATION,
        Permission.RECORD_AUDIO,
        Permission.READ_PHONE_STATE,
        Permission.CALL_PHONE,
        Permission.ADD_VOICEMAIL,
        Permission.USE_SIP,
        Permission.READ_PHONE_NUMBERS,
        Permission.ANSWER_PHONE_CALLS,
        Permission.READ_CALL_LOG,
        Permission.WRITE_CALL_LOG,
        Permission.PROCESS_OUTGOING_CALLS,
        Permission.BODY_SENSORS,
        Permission.ACTIVITY_RECOGNITION,
        Permission.SEND_SMS,
        Permission.RECEIVE_SMS,
        Permission.READ_SMS,
        Permission.RECEIVE_WAP_PUSH,
        Permission.RECEIVE_MMS,
        Permission.READ_EXTERNAL_STORAGE,
        Permission.WRITE_EXTERNAL_STORAGE,
        Permission.SYSTEM_ALERT_WINDOW,
        Permission.WRITE_SETTINGS,
        Permission.MANAGE_EXTERNAL_STORAGE,
        Permission.REQUEST_INSTALL_PACKAGES
})
@Retention(RetentionPolicy.SOURCE)
public @interface PermissionDef{
}
