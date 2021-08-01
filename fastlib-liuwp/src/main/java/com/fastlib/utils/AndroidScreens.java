package com.fastlib.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Modified by liuwp on 2019/3/22.
 * 如果main方法可以正常运行，直接在对应目录下生成屏幕适配文件。
 */
public class AndroidScreens {
    private static int width = 720;//单位px;根据这个基准值生成适配文件，该值来源于APP的UI设计图中标注的屏幕宽度。
    private static int graduationNum = 1000;//刻度数
    private static File rootFile;

    static {
        rootFile = new File("E:/Liuwp_project_open_source/Fastlib/app/src/main/res");
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
    }

    public static void main(String[] args) {
        //533,592,600,640,662,720,768,800,811,820,960,961,1024,1280,1365（平板和TV）
        //320,360,384,392,400,410,411,432,480（手机）
        //Auto-generated method
        createDp(new SimpleConvert(320), graduationNum);
        createDp(new SimpleConvert(360), graduationNum);
        createDp(new SimpleConvert(384), graduationNum);
        createDp(new SimpleConvert(411), graduationNum);
        createDp(new SimpleConvert(432), graduationNum);
        createDp(new SimpleConvert(480), graduationNum);
        createDp(new SimpleConvert(600), graduationNum);
        createDp(new SimpleConvert(800), graduationNum);

        //createDp(new SimpleConvert(240), graduationNum);
        //createDp(new SimpleConvert(320), graduationNum);
        //createDp(new SimpleConvert(360), graduationNum);
        //createDp(new SimpleConvert(384), graduationNum);
        //createDp(new SimpleConvert(411), graduationNum);
        //createDp(new SimpleConvert(432), graduationNum);
        //createDp(new SimpleConvert(480), graduationNum);
        //createDp(new SimpleConvert(533), graduationNum);
        //createDp(new SimpleConvert(592), graduationNum);
        //createDp(new SimpleConvert(600), graduationNum);
        //createDp(new SimpleConvert(640), graduationNum);
        //createDp(new SimpleConvert(662), graduationNum);
        //createDp(new SimpleConvert(720), graduationNum);
        //createDp(new SimpleConvert(768), graduationNum);
        //createDp(new SimpleConvert(800), graduationNum);
        //createDp(new SimpleConvert(811), graduationNum);
        //createDp(new SimpleConvert(820), graduationNum);
        //createDp(new SimpleConvert(960), graduationNum);
        //createDp(new SimpleConvert(961), graduationNum);
        //createDp(new SimpleConvert(1024), graduationNum);
        //createDp(new SimpleConvert(1280), graduationNum);
        //createDp(new SimpleConvert(1365), graduationNum);
    }

    private static void createDp(Convert convert, int graduationNum) {
        StringBuilder sb = new StringBuilder();
        sb.append("<resources xmlns:tools=\"http://schemas.android.com/tools\" tools:ignore=\"MissingDefaultResource\">");
        sb.append("\r\n");
        sb.append(convert.getTips());
        sb.append("\r\n");
        for (int i = 1; i <= graduationNum; i++) {
            sb.append("\t");
            sb.append("<dimen name=\"q");
            sb.append(i);
            sb.append("\">");
            sb.append(convert.convert(i));
            sb.append("dp</dimen>");
            sb.append("\r\n");

            if(i>=10&&i<=100){
                sb.append("\t");
                sb.append("<dimen name=\"s");
                sb.append(i);
                sb.append("\">");
                sb.append(convert.convert(i));
                sb.append("sp</dimen>");
                sb.append("\r\n");
            }
        }
        sb.append("</resources>");
        saveStringToFile(sb.toString(), convert.getSaveFilePath());
    }


    private static void saveStringToFile(String str, String filePath) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            fos.write(str.getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos!=null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public interface Convert {
        float convert(int px);
        String getSaveFilePath();
        String getTips();
    }

    public static class SimpleConvert implements Convert {
        private int shortWidthDP;
        private float scale;

        public SimpleConvert(int shortWidthDP) {
            super();
            this.shortWidthDP = shortWidthDP;
            this.scale = ((float) shortWidthDP) / ((float) width);
        }

        @Override
        public float convert(int px) {
            return (float) Math.round(((float) px) * scale * 100f) / 100f;
        }

        @Override
        public String getSaveFilePath() {
            return getFilePath("values-sw" + shortWidthDP + "dp");
        }

        @Override
        public String getTips() {
            //计算屏幕像素密度.
            float density = ((float) width)/((float) shortWidthDP)*160;
            return "<!-- "+"width:"+width+"px smallestWidth:"+shortWidthDP+"dp   "+density+"dpi -->";
        }
    }

    private static String getFilePath(String fileParent) {
        File file = new File(rootFile, fileParent);
        if (!file.exists()) {
            file.mkdirs();
        }
        File dimesFile = new File(file, "dimens.xml");
        return dimesFile.getAbsolutePath();
    }
}