package com.fastlib.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by liuwp on 2018/1/24.
 * 图片压缩辅助类
 */
public class ImageCompressor {
    //max width and height values of the compressed image is taken as 612x816
    private int maxWidth = 612;
    private int maxHeight = 816;
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int quality = 90;
    private String destinationDirectoryPath;

    private ImageCompressor(Context context) {
        destinationDirectoryPath = context.getCacheDir().getPath() + File.separator + "images";
    }
    private static ImageCompressor compressor;

    public static ImageCompressor getInstance(Context context){
        if(compressor == null){
            compressor = new ImageCompressor(context);
        }
        return compressor;
    }

    public ImageCompressor setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public ImageCompressor setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public ImageCompressor setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
        return this;
    }

    public ImageCompressor setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public ImageCompressor setDestinationDirectoryPath(String destinationDirectoryPath) {
        this.destinationDirectoryPath = destinationDirectoryPath;
        return this;
    }

    public File compressToFile(File imageFile){
        return compressToFile(imageFile, imageFile.getName());
    }

    public File compressToFile(File imageFile, String compressedFileName){
        return ImageUtil.compressImage(imageFile, maxWidth, maxHeight, compressFormat, quality,
                destinationDirectoryPath + File.separator + compressedFileName);
    }
}
