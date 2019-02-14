package com.fastlib.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

/**
 * Notification统一管理类
 */
public class N {

    private N() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void showShort(Context context, CharSequence message) {
        showToastShort(context, message);
    }

    public static void showLong(Context context, int message) {
        showToastLong(context, message);
    }

    public static void showLong(Context context, CharSequence message) {
        showToastLong(context, message);
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showToastShort(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showToastShort(Context context, int message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showToastLong(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showToastLong(Context context, int message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 自定义时间显示Toast
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void showToast(Context context, CharSequence message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    /**
     * 自定义时间显示Toast
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void showToast(Context context, int message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    /**
     * 短时间显示Snackbar
     *
     * @param view
     * @param message
     */
    public static void showSnackbarShort(View view, CharSequence message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * 短时间显示Snackbar，有监听
     *
     * @param view
     * @param message
     * @param actionMessage
     * @param listener
     */
    public static void showSnackbarShort(View view, CharSequence message, CharSequence actionMessage, View.OnClickListener listener) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction(actionMessage, listener).show();
    }

    /**
     * 短时间显示Snackbar
     *
     * @param view
     * @param message
     */
    public static void showSnackbarShort(View view, int message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * 长时间显示Snackbar
     *
     * @param view
     * @param message
     */
    public static void showSnackbarLong(View view, CharSequence message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Snackbar
     *
     * @param view
     * @param message
     */
    public static void showSnackbarLong(View view, int message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Snackbar，有监听
     *
     * @param view
     * @param message
     * @param actionMessage
     * @param listener
     */
    public static void showSnackbarLong(View view, CharSequence message, CharSequence actionMessage, View.OnClickListener listener) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction(actionMessage, listener);
    }

    /**
     * 显示通知,适配Android 8.0及以上版本。
     *
     * @param context
     * @param id
     * @param icon
     * @param title
     * @param message
     * @param channelId   渠道id，例：chat
     * @param channelName 渠道名称，例：聊天消息
     * @param importance  重要等级，例：NotificationManager.IMPORTANCE_HIGH
     */
    public static void showNotify(Context context, int id, @DrawableRes int icon, CharSequence title, CharSequence message, String channelId, String channelName, int importance) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager != null) {
                manager.createNotificationChannel(new NotificationChannel(channelId, channelName, importance));
            }
//            //检测通知渠道是否关闭
//            if (manager != null && manager.getNotificationChannel(channelId).getImportance() == NotificationManager.IMPORTANCE_NONE) {
//                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
//                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
//                intent.putExtra(Settings.EXTRA_CHANNEL_ID,manager.getNotificationChannel(channelId).getId());
//                context.startActivity(intent);
//            }
        }
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .build();
        if (manager != null) {
            manager.notify(id, notification);
        }
    }
}