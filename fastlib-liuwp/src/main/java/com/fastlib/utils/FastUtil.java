package com.fastlib.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuwp on 2020/8/27.
 * Modified by liuwp on 2021/5/25.
 * 通用工具类。
 */
public class FastUtil {

    private FastUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * @param context 上下文
     * @return 与AndroidManifest.xml文件中FileProvider的属性android:authorities="xxx"值一致.
     */
    public static String getFileProviderAuthority(@NonNull Context context){
        return context.getPackageName()+".fastfileprovider";
    }

    /**
     * 创建指定父级、文件名的空文件(如果文件已存在，会先删除后重新创建)
     *
     * @param parent 父级目录
     * @param fileName 文件名。例app.apk
     * @return 空文件
     */
    public static File createEmptyFile(@NonNull Context context, @Nullable File parent, @Nullable String fileName) {
        File file = null;
        if (TextUtils.isEmpty(fileName)) fileName = String.valueOf(System.currentTimeMillis());
        if (parent != null && parent.exists())
            file = new File(parent, fileName);
        else {
            File dir = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                dir = context.getExternalFilesDir(null);
            } else {
                dir = context.getFilesDir();
            }
            file = new File(dir, fileName);
        }
        if(file.exists())
            file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 保存图片到相册,需要权限：android.permission.WRITE_EXTERNAL_STORAGE
     *
     * @param context 上下文
     * @param bitmap 位图，例：BitmapFactory.decodeResource(resources, R.drawable.image)
     * @param fileName 文件名，例：image.jpeg
     * @param compressFormat 压缩格，例：Bitmap.CompressFormat.JPEG
     * @return true成功保存图片到相册 false图片保存失败
     */
    public static boolean saveBitmapToAlbum(Context context, Bitmap bitmap, String fileName, Bitmap.CompressFormat compressFormat) {
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + compressFormat.toString());
        values.put(MediaStore.Files.FileColumns.WIDTH, bitmap.getWidth());
        values.put(MediaStore.Files.FileColumns.HEIGHT, bitmap.getHeight());
        if (Build.VERSION.SDK_INT >= 29) {
            values.put("relative_path", Environment.DIRECTORY_PICTURES);
        } else {
            values.put(MediaStore.MediaColumns.DATA, Environment.getExternalStorageDirectory().getPath() + File.separator +
                    Environment.DIRECTORY_PICTURES + File.separator + fileName);
        }
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(compressFormat, 100, outputStream);
                    outputStream.close();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 快速生成 集合
     *
     * @param values
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... values) {
        List<T> list = new ArrayList<>();
        if (values == null)
            return list;
        list.addAll(Arrays.asList(values));
        return list;
    }

    /**
     * 安全转换字符串为整型
     *
     * @param value
     * @param defValue
     * @return 转换失败返回默认值
     */
    public static int safeToString(String value, int defValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    /**
     * 安全转换字符串为长整型
     *
     * @param value
     * @param defValue
     * @return 转换失败返回默认值
     */
    public static long safeToString(String value, long defValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    /**
     * 安全转换字符串为单精浮点型
     *
     * @param value
     * @param defValue
     * @return 转换失败返回默认值
     */
    public static float safeToString(String value, float defValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    /**
     * 安全转换字符串为双精浮点型
     *
     * @param value
     * @param defValue
     * @return 转换失败返回默认值
     */
    public static double safeToString(String value, double defValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    /**
     * Drawable染色
     *
     * @param src
     * @param color
     * @return
     */
    public static Drawable tintDrawable(@NonNull Drawable src, @ColorInt int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(src.mutate());
        DrawableCompat.setTint(wrapDrawable, color);
        return wrapDrawable;
    }

    /**
     * 字符串部分字染色
     *
     * @param rawText
     * @param start
     * @param end
     * @param color
     * @return
     */
    public static SpannableStringBuilder tintText(String rawText, int start, int end, int color) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(rawText);
        ForegroundColorSpan span = new ForegroundColorSpan(color);
        ssb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    /**
     * 字符串部分字放大、缩小
     *
     * @param rawText
     * @param start
     * @param end
     * @param proportion 缩放比例
     * @return
     */
    public static SpannableStringBuilder zoomText(String rawText, int start, int end, float proportion) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(rawText);
        RelativeSizeSpan span = new RelativeSizeSpan(proportion);
        ssb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    /**
     * 开始倒计时
     * 优势：...
     * 缺陷：用户界面销毁后，计时会被中断。
     * 优化方案：使用倒计时服务来实现倒计时，可以防止计时中断。
     *          @see com.fastlib.base.custom.CountDownService
     * @param countDownTimer 倒计时器
     */
    @Deprecated
    public static void startCountDown(CountDownTimer countDownTimer) {
        countDownTimer.start();
    }

    /**
     * 取消倒计时
     * @param countDownTimer 倒计时器
     */
    @Deprecated
    public static void cancelCountDown(CountDownTimer countDownTimer) {
        countDownTimer.cancel();
    }
}