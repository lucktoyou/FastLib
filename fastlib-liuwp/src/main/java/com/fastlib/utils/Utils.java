package com.fastlib.utils;

import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by sgfb on 16/7/11.
 *
 * 公共工具类
 */
public class Utils {

    private Utils(){
        //no instance
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... values){
        List<T> list=new ArrayList<>();
        if(values==null)
            return list;
        list.addAll(Arrays.asList(values));
        return list;
    }

    /**
     * 安全转换字符串为整型
     * @param value
     * @param defValue
     * @return 转换失败返回默认值
     */
    public static int safeToString(String value,int defValue){
        try{
            return Integer.parseInt(value);
        }catch (NumberFormatException e){
            return defValue;
        }
    }

    /**
     * 安全转换字符串为长整型
     * @param value
     * @param defValue
     * @return 转换失败返回默认值
     */
    public static long safeToString(String value,long defValue){
        try{
            return Long.parseLong(value);
        }catch (NumberFormatException e){
            return defValue;
        }
    }

    /**
     * 安全转换字符串为单精浮点型
     * @param value
     * @param defValue
     * @return 转换失败返回默认值
     */
    public static float safeToString(String value,float defValue){
        try{
            return Float.parseFloat(value);
        }catch (NumberFormatException e){
            return defValue;
        }
    }

    /**
     * 安全转换字符串为双精浮点型
     * @param value
     * @param defValue
     * @return 转换失败返回默认值
     */
    public static double safeToString(String value,double defValue){
        try{
            return Double.parseDouble(value);
        }catch (NumberFormatException e){
            return defValue;
        }
    }

    /**
     * Drawable染色
     * @param src
     * @param color
     * @return
     */
    public static Drawable tintDrawable(Drawable src, int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(src.mutate());
        DrawableCompat.setTint(wrapDrawable,color);
        return wrapDrawable;
    }

    /**
     * 字符串部分字染色
     * @param start
     * @param end
     * @param text
     * @param color
     * @return
     */
    public static SpannableStringBuilder getTextSomeOtherColor(int start, int end, String text, int color) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ForegroundColorSpan foregroundColor = new ForegroundColorSpan(color);
        ssb.setSpan(foregroundColor,start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    /**
     * 获取某数的二进制的具体几位
     * @param raw
     * @param start
     * @param end
     * @return
     */
    public static int getSomeBits(long raw,int start,int end){
        int flag=0;
        if(start>end)
            return flag;
        for(int i=start;i<end;i++)
            flag|=(int) Math.pow(2,64-i);
        flag&=raw;
        return flag;
    }

    /**
     * 多字节转数字。默认小数端,最多处理8字节
     * @param bytes
     * @return
     */
    public static long bytesToNumber(byte... bytes){
        long number=0;
        for(int i=0;i<bytes.length;i++){
            long temp=bytes[i];
            number+=temp<<(i*8);
        }
        return number;
    }

    public static int bytesToInt(byte... data){
        int var=0;
        for(int i=0;i<data.length;i++){
            int middle=data[i];
            var|=((0xff&middle)<<i*8);
        }
        return var;
    }

    /**
     * 对字符串进行MD5加密
     * @param source 需要进行加密的字符串
     * @param is16bits 加密长度,true为16位反之32位
     * @return MD5加密后字符串
     */
    public static String getMd5(String source,boolean is16bits){
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(source.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        if(is16bits)
            return md5StrBuff.substring(8, 24).toUpperCase(Locale.getDefault()); //16位加密，从第9位到25位
        else
            return md5StrBuff.toString();
    }

    /**
     * sha1文件检验
     * @param filePath 文件路径
     * @param type 文件校验类型
     * @return sha1文件校验码
     */
    public static String getFileVerify(String filePath,FileVerifyType type){
        String typeStr="SHA-1";
        switch (type){
            case SHA_1:typeStr="SHA-1";break;
            case MD5:typeStr="MD5";break;
        }
        try {
            FileInputStream in = new FileInputStream(new File(filePath));
            MessageDigest md = MessageDigest.getInstance(typeStr);
            byte[] data = new byte[8192];
            int len;
            while((len=in.read(data))!=-1)
                md.update(data,0,len);
            data=md.digest();
            in.close();
            StringBuilder  sb= new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                if (Integer.toHexString(0xFF & data[i]).length() == 1)
                    sb.append("0").append(Integer.toHexString(0xFF & data[i]));
                else
                    sb.append(Integer.toHexString(0xFF & data[i]));
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件校验类型
     */
    public enum FileVerifyType{
        SHA_1,
        MD5,
    }
}