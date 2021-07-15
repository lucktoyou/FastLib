package com.fastlib.net.tool;

import android.text.TextUtils;

/**
 * Created by sgfb on 2019/12/3
 * E-mail:602687446@qq.com
 * URL解析工具
 */
public class URLUtil {

    private URLUtil(){
        //no instance
    }

    /**
     * 获取host和端口后的url端
     * @param url   例 1:http://www.baidu.com/getTop 2:http://www.baidu.com
     * @return  例 1:/getTop 2:/
     */
    public static String getPath(String url){
        final String fDefaultPath="/";
        int notSlashIndex=getNotSlashIndex(url);

        if(notSlashIndex<0) return fDefaultPath;
        if(notSlashIndex<url.length()-1){
            int pathIndex=url.substring(notSlashIndex).indexOf('/');
            if(pathIndex!=-1) return url.substring(notSlashIndex+pathIndex);
        }
        return fDefaultPath;
    }

    private static int getNotSlashIndex(String url){
        int colonIndex=url.indexOf(':');
        if(colonIndex==-1||colonIndex>=url.length()-1) return -1;

        //取冒号后非斜杠索引
        int notSlashIndex=colonIndex;
        char colonBehind;
        do{
            colonBehind=url.charAt(++notSlashIndex);
        }while(colonBehind=='/'&&notSlashIndex<url.length());
        return notSlashIndex;
    }

    /**
     * 获取Host
     * @param url   完整的url 例:http://www.baidu.com:8080/getTop
     * @return  协议后首个冒号或者斜杠的位置,取这之间的字符串.否则返回协议后所有字符串
     */
    public static String getHost(String url){
        int notSlashIndex=getNotSlashIndex(url);

        if(notSlashIndex<0) return null;

        int notSlashToColonIndex=url.substring(notSlashIndex).indexOf(':');
        if(notSlashToColonIndex!=-1) return url.substring(notSlashIndex,notSlashIndex+notSlashToColonIndex);

        int notSlashToNextSlashIndex=url.substring(notSlashIndex).indexOf('/');
        if(notSlashToNextSlashIndex!=-1) return url.substring(notSlashIndex,notSlashIndex+notSlashToNextSlashIndex);
        return url.substring(notSlashIndex);
    }

    /**
     * 获取端口号
     * @param url   完整的url 例:http://www.baidu.com:8080/getTop
     * @return  取协议后首个冒号后数字 如果没有则取80
     */
    public static int getPort(String url) {
        int notSlashIndex = getNotSlashIndex(url);
        if (notSlashIndex < 0) return resultDefaultPort(url);

        int notSlashToColonIndex = url.substring(notSlashIndex).indexOf(':');
        if (notSlashToColonIndex != -1) {
            int numberLength = 0;
            int index = 0;
            int colonIndex=notSlashIndex+notSlashToColonIndex+1;
            while (colonIndex + index < url.length()) {
                char c = url.charAt(colonIndex + index++);
                if (c >= '0' && c <= '9')
                    numberLength++;
                else break;
            }
            if (numberLength > 0) {
                String number = url.substring(colonIndex, colonIndex + numberLength);
                return Integer.parseInt(number);
            }
        }
        return resultDefaultPort(url);
    }

    public static String getHostAndPort(String url){
        return getHost(url)+":"+getPort(url);
    }

    private static int resultDefaultPort(String url){
        final int fDefaultPort = 80;
        final int fDefaultHttpsPort=443;
        return url.startsWith("https")?fDefaultHttpsPort:fDefaultPort;
    }

    public static boolean validUrl(String url){
        return !TextUtils.isEmpty(url)&&
                (url.startsWith("http://")||url.startsWith("https://"))&&
                ((url.startsWith("http://")&&url.length()>=8)||(url.startsWith("https://")&&url.length()>=9));
    }
}
