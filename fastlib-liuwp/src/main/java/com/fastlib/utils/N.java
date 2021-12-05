package com.fastlib.utils;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fastlib.utils.core.SaveUtil;
import com.google.android.material.snackbar.Snackbar;

/**
 * Created by liuwp on 2020/11/17.
 * Notification统一管理类.
 */
public class N {

    private N() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 显示Toast
     *
     * @param context
     * @param message
     */
    public static void showToast(Context context, CharSequence message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * 显示SnackBar
     *
     * @param view
     * @param message
     * @param actionMessage
     * @param listener
     */
    public static void showSnackBar(View view, CharSequence message, CharSequence actionMessage, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction(actionMessage, listener);
        snackbar.show();
    }

    /**
     * 显示通知,支持点击跳转,适配Android 8.0及以上版本。
     * 使用默认的声音、振动、闪光;震动需添加权限:<uses-permission android:name="android.permission.VIBRATE"/>
     * 点击“不再提示”按钮，通知开关关闭后不再提示。
     *
     * @param context
     * @param id
     * @param icon
     * @param title
     * @param message
     * @param channelId     渠道id，例：chat
     * @param channelName   渠道名称，例：聊天消息
     * @param importance    重要等级，例：NotificationManager.IMPORTANCE_HIGH
     * @param pendingIntent 例：PendingIntent.getActivities(context, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT)
     */
    public static void showNotify(final Context context, int id, @DrawableRes int icon, CharSequence title, CharSequence message, final String channelId, String channelName, int importance, PendingIntent pendingIntent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final String ALL_NOTIFICATION_NOT_ENABLED = "allNotificationNotEnabled";
        final String CHANNEL_NOTIFICATION_NOT_ENABLED = channelId + "NotificationNotEnabled";
        //创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            manager.createNotificationChannel(channel);
        }
        //检测通知是否开启.注意这个方法判断的是通知总开关，如果APP通知被关闭，则其下面的所有通知渠道也被关闭.
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled() && SaveUtil.getFromSp(ALL_NOTIFICATION_NOT_ENABLED, true)) {
            new AlertDialog.Builder(context)
                    .setTitle("提示")
                    .setMessage("是否开启通知？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //打开通知设置页面
                            String packageName = context.getPackageName();
                            int uid = context.getApplicationInfo().uid;
                            Intent intent = new Intent();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
                                intent.putExtra(Settings.EXTRA_CHANNEL_ID, uid);
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                                intent.putExtra("app_package", packageName);
                                intent.putExtra("app_uid", uid);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setData(Uri.parse("package:" + packageName));
                            } else {
                                intent.setAction(Settings.ACTION_SETTINGS);
                            }
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("不再提示", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SaveUtil.saveToSp(ALL_NOTIFICATION_NOT_ENABLED, false);
                        }
                    })
                    .show();
            return;
        }
        //检测某一渠道的通知是否开启.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(channelId).getImportance() == NotificationManager.IMPORTANCE_NONE && SaveUtil.getFromSp(CHANNEL_NOTIFICATION_NOT_ENABLED, true)) {
            new AlertDialog.Builder(context)
                    .setTitle("提示")
                    .setMessage("是否开启“" + channelName + "”渠道通知？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("不再提示", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SaveUtil.saveToSp(CHANNEL_NOTIFICATION_NOT_ENABLED, false);
                        }
                    })
                    .show();
            return;
        }
        //通知内容
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .build();
        //发送通知
        manager.notify(id, notification);
    }
}