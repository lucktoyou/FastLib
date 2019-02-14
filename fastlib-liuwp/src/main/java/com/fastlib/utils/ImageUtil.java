package com.fastlib.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liuwp on 2018/1/24.
 */
public class ImageUtil{
    private static final int REQUEST_FROM_ALBUM=10000;
    private static final int REQUEST_FROM_CAMERA=10001;
    private static final int REQUEST_FROM_CROP=10002;
    private static Uri mImageUri;
    private static File mImageFile;
    private ImageUtil(){
        //不实例化
    }

    public static void openAlbum(Activity activity){
        openAlbum(activity, null);
    }

    public static void openAlbum(Fragment fragment){
        openAlbum(null, fragment);
    }

    /**
     * 打开相册
     * @param activity
     */
    private static void openAlbum(Activity activity,Fragment fragment){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.CATEGORY_OPENABLE, true);
        if (activity != null)
            activity.startActivityForResult(intent, REQUEST_FROM_ALBUM);
        else
            fragment.startActivityForResult(intent, REQUEST_FROM_ALBUM);
    }


    public static void openCamera(Activity activity){
        mImageFile = getTempFile("",".jpg",null);//默认写在存储卡内
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){//适配7.0
            mImageUri = FileProvider.getUriForFile(activity,getFileProviderAuthority(activity),mImageFile);
        }else{
            mImageUri = Uri.fromFile(mImageFile);
        }
        openCamera(activity,mImageFile);
    }

    public static void openCamera(Fragment fragment){
        mImageFile = getTempFile("",".jpg",null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mImageUri = FileProvider.getUriForFile(fragment.getContext(),getFileProviderAuthority(fragment.getContext()),mImageFile);
        }else{
            mImageUri = Uri.fromFile(mImageFile);
        }
        openCamera(fragment,mImageFile);
    }

    public static void openCamera(Activity activity,File file){
        mImageFile = file;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mImageUri = FileProvider.getUriForFile(activity,getFileProviderAuthority(activity),file);
        }else{
            mImageUri = Uri.fromFile(file);
        }
        openCamera(activity,null,mImageUri);
    }

    public static void openCamera(Fragment fragment,File file){
        mImageFile = file;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mImageUri = FileProvider.getUriForFile(fragment.getContext(),getFileProviderAuthority(fragment.getContext()),file);
        }else{
            mImageUri = Uri.fromFile(file);
        }
        openCamera(null,fragment,mImageUri);
    }

    /**
     * 打开相机,不指定生成照片位置
     * @param activity
     */
    private static void openCamera(Activity activity,Fragment fragment,Uri outPut){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//7.0以上需添加标志，以下可不加
        intent.putExtra(MediaStore.EXTRA_OUTPUT,outPut);
        if(activity!=null)
            activity.startActivityForResult(intent,REQUEST_FROM_CAMERA);
        else
            fragment.startActivityForResult(intent,REQUEST_FROM_CAMERA);
    }

    /**
     * 获取来自相册或者手机拍摄的照片uri.如果授权失败或者没有选择任何照片，返回null
     * 7.0以上（包含7.0）uri类型为content://,以下uri类型为file:///
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */
    public static Uri getImageFromActive(int requestCode, int resultCode, Intent data){
        if(resultCode!=Activity.RESULT_OK){
            return null;
        }
        switch (requestCode){
            case REQUEST_FROM_CAMERA:
                return mImageUri;
            case REQUEST_FROM_ALBUM:
                return data.getData();
            default:
                return null;
        }
    }

    public static void startActionCrop(Activity activity, Uri uri,Uri outPut){
        startActionCrop(activity,uri,outPut,1,1,800,800,REQUEST_FROM_CROP);
    }

    /**
     * 裁剪图片
     * @param activity
     * @param uri
     * @param outPut
     */
    private static void startActionCrop(Activity activity,Uri uri,Uri outPut,int aspectX,int aspectY,int outputX,int outputY,int requestCode){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// 7.0以上需添加标志，以下不需要
        intent.setDataAndType(uri, "image/*");// uri类型file:///或content：//
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);// 裁剪框的比例
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);// 裁剪后输出图片的尺寸大小
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPut);// outPut类型file:///（content：//类型返回,试了不行）
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // 取消人脸识别
        activity.startActivityForResult(intent, requestCode);
    }
    
    public static String getFileProviderAuthority(Context context){
        //返回值与AndroidManifest.xml文件中FileProvider 的属性android:authorities="${applicationId}.fileprovider"值一致
        return context.getPackageName()+".fileprovider";
    }

    /**
     * 创建可指定父级和前缀的临时文件
     * @param prefix 前缀
     * @param suffix 后缀
     * @param parent 父级目录
     * @return
     */
    public static File getTempFile(@Nullable String prefix,@Nullable String suffix,@Nullable File parent){
        File file=null;

        if(prefix==null) prefix="";
        if(suffix==null) suffix="";
        if(parent!=null&&parent.exists())
            file=new File(parent,prefix+System.currentTimeMillis()+suffix);
        else{
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                file=new File(Environment.getExternalStorageDirectory(),prefix+System.currentTimeMillis()+suffix);
        }
        if(file!=null)
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return file;
    }

    /**
     * 根据提供的Uri解析出文件绝对路径
     * 7.0以上（包含7.0）uri类型为content://,以下uri类型为file:///
     * @param uri
     * @return
     */
    public static String getImageAbsolutePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String path = null;
        if (scheme == null)
            path = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            // uri类型为file:///
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            /* uri类型为content://
                例：uri = content://com.hzhanghuan.policingadmin.fileprovider/camera_photos/1514445852792.jpg
                实际路径：/storage/emulated/0/1514445852792.jpg
             */
            //应对相册
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        path = cursor.getString(index);
                    }
                }
                cursor.close();
            }
            //应对相机
            if(path == null && mImageFile != null){
                path = mImageFile.getAbsolutePath();
            }
        }
        return path;
    }

    /**
     * ①直接使用该方法来压缩图片；
     * ②使用ImageCompressor辅助类压缩(建议使用)。
     * @param imageFile 原图文件
     * @param reqWidth 宽度（并不是最终返回图片的宽度，而是用于计算图片采样比例，控制图片缩小倍数）
     * @param reqHeight 高度
     * @param compressFormat 图片格式
     * @param quality 质量
     * @param destinationPath 压缩后图片保存路径
     * @return 压缩后的图片文件
     */
    public static File compressImage(File imageFile, int reqWidth, int reqHeight, Bitmap.CompressFormat compressFormat, int quality, String destinationPath){
        FileOutputStream fileOutputStream = null;
        File file = new File(destinationPath).getParentFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            fileOutputStream = new FileOutputStream(destinationPath);
            // write the compressed bitmap at the destination specified by destinationPath.
            decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight).compress(compressFormat, quality, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new File(destinationPath);
    }

    //获取缩略图
    private static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight){
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        //check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scaledBitmap;
    }

    //计算缩略比例
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
