package com.fastlib.utils;

import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuwp on 2018/10/18.
 * <p>
 * 用于文件操作的工具类
 */
public class FileUtil {

    private final static int BUFFER = 8192;
    private final static long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;//一天毫秒数

    //SD卡是否存在
    public static boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    //存储空间，总大小
    public static long getStorageAll(File dir) {
        long size = 0;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            try {
                StatFs stat = new StatFs(dir.getPath());
                size = (long) stat.getBlockSize() * stat.getBlockCount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    //存储空间，可用大小
    public static long getStorageAvailable(File dir) {
        long size = 0;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            try {
                StatFs stat = new StatFs(dir.getPath());
                size = (long) stat.getBlockSize() * stat.getAvailableBlocks();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    //存储空间，剩余大小
    public static long getStorageFree(File dir) {
        long size = 0;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            try {
                StatFs stat = new StatFs(dir.getPath());
                size = (long) stat.getBlockSize() * stat.getFreeBlocks();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    //存储空间，已用大小
    public static long getStorageUsed(File dir) {
        long size = 0;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            try {
                StatFs stat = new StatFs(dir.getPath());
                size = (long) stat.getBlockSize() * (stat.getBlockCount() - stat.getFreeBlocks());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    //获取目录大小
    public static long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getDirSize(file);
                    }
                }
            }
        }
        return size;
    }

    //获取文件大小
    public static long getFileSize(File file) throws IOException {
        long size = 0;
        if (file.exists()) {
            size = new FileInputStream(file).available();
        }
        return size;
    }

    //复制文件或目录
    public static void copy(File sourceFile, File targetFile) throws IOException {
        if (sourceFile != null) {
            if (sourceFile.isFile()) {
                copyFile(sourceFile, targetFile);
            } else {
                copyDirectory(sourceFile, targetFile);
            }
        }

    }

    //复制文件
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        if (sourceFile != null && targetFile != null) {
            try {
                inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
                outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
                byte[] buffer = new byte[BUFFER];
                int length;
                while ((length = inBuff.read(buffer)) != -1) {
                    outBuff.write(buffer, 0, length);
                }
                outBuff.flush();//清空缓冲区数据,防止数据丢失。
            } finally {
                if (inBuff != null) {
                    inBuff.close();
                }
                if (outBuff != null) {
                    outBuff.close();
                }
            }
        }
    }

    //复制文件夹
    public static void copyDirectory(File sourceDir, File targetDir) throws IOException {
        if (sourceDir != null && targetDir != null) {
            //遍历源目录下所有文件或目录
            File[] file = sourceDir.listFiles();
            for (int i = 0; i < file.length; i++) {
                if (file[i].isFile()) {
                    File sourceFile = file[i];
                    File targetFile = new File(targetDir.getAbsolutePath() + File.separator + file[i].getName());
                    copyFile(sourceFile, targetFile);
                } else if (file[i].isDirectory()) {
                    File dir1 = new File(sourceDir, file[i].getName());
                    File dir2 = new File(targetDir, file[i].getName());
                    copyDirectory(dir1, dir2);
                }
            }
        }
    }

    //删除文件或目录
    public static boolean delete(File file) {
        if (file != null) {
            if (file.isFile()) {
                return deleteFile(file);
            } else {
                return deleteDirectory(file, true);
            }
        }
        return false;
    }

    //删除文件
    public static boolean deleteFile(File file) {
        if (file != null && file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    //删除目录
    public static boolean deleteDirectory(File dirFile, boolean includeSelf) {
        return deleteDirectory(dirFile, null, includeSelf, false);
    }

    //删除目录
    public static boolean deleteDirectory(File dirFile, String extension, boolean includeSelf, boolean onlyFile) {
        if (dirFile == null || !dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if (extension == null || files[i].getName().toLowerCase().endsWith("." + extension.toLowerCase())) {
                        flag = deleteFile(files[i]);
                        if (!flag) {
                            break;
                        }
                    }
                } else {
                    if (!onlyFile) {
                        flag = deleteDirectory(files[i], true);
                        if (!flag) {
                            break;
                        }
                    }
                }
            }
        }

        if (!flag) {
            return false;
        }

        if (includeSelf) {
            return dirFile.delete();
        } else {
            return true;
        }
    }

    //按照最后修改时间删除文件
    public static boolean deleteDirectoryByTime(File dirFile, int day) {
        if (dirFile == null || !dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                long time = System.currentTimeMillis() - file.lastModified() - day * ONE_DAY_MILLIS;
                if (time > 0) {
                    if (file.isDirectory()) {
                        flag = deleteDirectory(file, true);
                    } else {
                        flag = delete(file);
                    }
                }
            }
        }
        return flag;
    }

    //移动文件或目录
    public static void move(File src, File dest) throws IOException {
        copy(src, dest);
        delete(src);
    }

    //从输入流读取文本内容
    public static String readTextInputStream(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        String line;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return sb.toString();
    }

    //从文件读取文本内容
    public static String readTextFile(File file) throws IOException {
        InputStream is = null;
        if (file != null) {
            try {
                is = new FileInputStream(file);
                return readTextInputStream(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return null;
    }

    //将文本内容写入文件
    public static void writeTextFile(File file, String str) throws IOException {
        DataOutputStream out = null;
        if (file != null) {
            try {
                out = new DataOutputStream(new FileOutputStream(file));
                out.write(str.getBytes());
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }


    //将一系列字符串写入文件
    public static void writeTextFile(File file, String[] strArray) throws IOException {
        if (file != null && strArray != null) {
            String str = "";
            for (int i = 0; i < strArray.length; i++) {
                str += strArray[i];
                if (i != strArray.length - 1) {
                    str += "\r\n";
                }
            }
            DataOutputStream out = null;
            try {
                out = new DataOutputStream(new FileOutputStream(file));
                out.write(str.getBytes());
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    //合并多个文本文件的内容到一个文件
    public static void combineTextFile(File[] sFiles, File dFile) throws IOException {
        BufferedReader in = null;
        BufferedWriter out = null;
        if (sFiles != null && dFile != null) {
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dFile)));

                for (int i = 0; i < sFiles.length; i++) {
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(sFiles[i])));
                    String oldLine = in.readLine();
                    String newLine = null;
                    while ((newLine = in.readLine()) != null) {
                        out.write(oldLine);
                        out.newLine();
                        oldLine = newLine;
                    }
                    out.write(oldLine);

                    if (i != sFiles.length - 1)
                        out.newLine();

                    out.flush();
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    //写入数据到文件
    public static void writeFile(File file, byte[] data) throws Exception {
        DataOutputStream out = null;
        if (file != null && data != null) {
            try {
                out = new DataOutputStream(new FileOutputStream(file));
                out.write(data);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    //将输入流中的数据写入文件
    public static long writeFile(File file, InputStream inStream) throws IOException {
        long dataSize = 0;
        DataInputStream in = null;
        DataOutputStream out = null;
        if (file != null && inStream != null) {
            try {
                in = new DataInputStream(inStream);
                out = new DataOutputStream(new FileOutputStream(file));
                byte buffer[] = new byte[BUFFER];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                    dataSize += length;
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
        return dataSize;
    }

    //读取文件到byte数组
    public static byte[] readFileToByte(File file) throws IOException {
        BufferedInputStream in = null;
        ByteArrayOutputStream out = null;
        if (file != null) {
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                out = new ByteArrayOutputStream((int) file.length());
                byte[] buffer = new byte[BUFFER];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                return out.toByteArray();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    out.close();
                }
            }
        }
        return null;
    }

    //字节数组转成流
    public static InputStream byteToInputSteram(byte[] data) {
        InputStream is = null;
        if (null != data && data.length > 0) {
            is = new ByteArrayInputStream(data);
        }
        return is;
    }

    /**
     * 获取单个文件的MD5值！
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 获取目录中文件的MD5值
     * @param file
     * @param listChild true递归子目录中的文件
     * @return
     */
    public static Map<String, String> getDirMD5(File file, boolean listChild) {
        if (file == null || !file.isDirectory()) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        String md5;
        File files[] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory() && listChild) {
                map.putAll(getDirMD5(f, true));
            } else {
                md5 = getFileMD5(f);
                if (md5 != null) {
                    map.put(f.getPath(), md5);
                }
            }
        }
        return map;
    }
}
