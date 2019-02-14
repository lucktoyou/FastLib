package com.fastlib.net;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.fastlib.app.EventObserver;
import com.fastlib.bean.EventDownloading;
import com.fastlib.bean.EventUploading;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 网络请求的具体处理.在结束时会保存一些状态<br/>
 * 这个类可以上传和下载文件,支持中断,下载文件时每秒发送一次进度广播(EventDownloading)
 */
public class NetProcessor implements Runnable {
    private final String BOUNDARY = Long.toHexString(System.currentTimeMillis());
    private final String CRLF = "\r\n";
    private final String END = "--" + BOUNDARY + "--" + CRLF;
    private final int BUFF_LENGTH = 4096;

    public static long mDiffServerTime; //与服务器时间差

    private boolean isSuccess = true;
    private long Tx, Rx;
    private byte[] mResponse;
    private Request mRequest;
    private String mMessage = null;
    private OnCompleteListener mListener;
    private Executor mResponsePoster;
    private IOException mException; //留存的异常

    public NetProcessor(Request request, OnCompleteListener l, final Handler handler) {
        mRequest = request;
        mListener = l;
        mResponsePoster = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        };
    }

    @Override
    public void run() {
        if (mRequest.getMock() != null) {
            mResponse = mRequest.getMock().dataResponse(mRequest);
            mMessage = "模拟数据";
            toggleCallback();
            if (mListener != null)
                mListener.onComplete(this);
            return;
        }
        long connectionTimer = System.currentTimeMillis();
        try {
            boolean isMulti = false, isPost = mRequest.getMethod().equals("POST") || mRequest.getMethod().equals("PUT"), needBody = (isPost || mRequest.getMethod().equals("GET")); //如果不是post类型也不是get，不需要请求体
            long existsLength;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            File downloadFile = null;
            InputStream in;
            OutputStream out;
            URL url = new URL(isPost ? mRequest.getUrl() : splicingGetUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (isPost && (mRequest.getFiles() != null && mRequest.getFiles().size() > 0))
                isMulti = true;
            //添加额外信息到头部
            if (mRequest.getSendHeadExtra() != null) {
                List<Request.ExtraHeader> list = mRequest.getSendHeadExtra();
                for (Request.ExtraHeader extra : list)
                    if (extra.canDuplication)
                        connection.addRequestProperty(extra.field, extra.value);
                    else connection.setRequestProperty(extra.field, extra.value);
            }
            //检测是否可保存为文件
            if (mRequest.downloadable()) {
                downloadFile = mRequest.getDownloadable().getTargetFile();
                existsLength = downloadFile.length();
                if (existsLength > 0 && mRequest.getDownloadable().supportBreak()) //如果支持中断并且文件已部分存在,跳过部分流
                    connection.addRequestProperty("Range", "bytes=" + Long.toString(existsLength) + "-");
                if (!TextUtils.isEmpty(mRequest.getDownloadable().expireTime())) //添加资源是否过期判断
                    connection.addRequestProperty("If-Modified-Since", mRequest.getDownloadable().expireTime());
            }
            connection.setRequestMethod(mRequest.getMethod());
            if (needBody)
                connection.setDoInput(true);
            if (isPost) {
                connection.setDoOutput(true);
                if (isMulti) {
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                    connection.setUseCaches(false);
                }
            }
            if (mRequest.getSendCookies() != null)
                connection.setRequestProperty("Cookie", generateSendCookies());
            if (Thread.currentThread().isInterrupted()) //在连接前判断线程是否已关闭
                return;
            connection.connect();
            if (isPost) {
                out = mRequest.isSendGzip() ? new GZIPOutputStream(connection.getOutputStream()) : connection.getOutputStream();
                if (isMulti)
                    multipart(mRequest.getParamsRaw(), mRequest.getFiles(), out);
                else {
                    //如果原始字节流存在，发送原始字节流，否则发送标准参数
                    byte[] rawBytes = mRequest.getByteStream();
                    if (rawBytes != null) {
                        if (rawBytes.length == 0) {
                            Gson gson = new Gson();
                            rawBytes = gson.toJson(mRequest.getParams()).getBytes();
                        }
                        if (rawBytes.length != 0) {
                            Tx += rawBytes.length;
                            out.write(rawBytes);
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        loadParams(mRequest.getParamsRaw(), sb);
                        byte[] data = sb.toString().getBytes();
                        Tx += data.length;
                        out.write(data);
                    }
                }
                out.close();
            }
            checkErrorStream(connection, connectionTimer); //判断返回码是否200.不是的话做额外处理
            if (needBody) {
                Context context = getHostContext();
                in = mRequest.isReceiveGzip() ? new GZIPInputStream(connection.getInputStream()) : connection.getInputStream();
                int len;
                byte[] data = new byte[BUFF_LENGTH];
                //如果支持,修改下载的文件名
                if (canWriteToFile(connection)) {
                    OutputStream fileOut = new FileOutputStream(downloadFile, mRequest.getDownloadable().supportBreak());
                    String disposition = connection.getHeaderField("Content-Disposition");
                    if (!TextUtils.isEmpty(disposition) && disposition.length() > 9 && mRequest.getDownloadable().changeIfHadName()) {
                        String filename = URLDecoder.decode(disposition.substring(disposition.indexOf("filename=") + 9), "UTF-8");
                        if (!TextUtils.isEmpty(filename))
                            downloadFile.renameTo(new File(downloadFile.getParent() + File.separator + filename));
                    }
                    int maxCount = connection.getContentLength();
                    int speed = 0;
                    long timer = System.currentTimeMillis();
                    while ((len = in.read(data)) != -1 && !Thread.currentThread().isInterrupted()) {
                        fileOut.write(data, 0, len);
                        Rx += len;
                        speed += len;
                        if (context != null && (System.currentTimeMillis() - timer) > 1000) { //每秒发送一次广播
                            EventObserver.getInstance().sendEvent(context, new EventDownloading(maxCount, speed, downloadFile.getAbsolutePath(), mRequest));
                            speed = 0;
                            timer = System.currentTimeMillis();
                        }
                    }
                    EventObserver.getInstance().sendEvent(context, new EventDownloading(maxCount, speed, downloadFile.getAbsolutePath(), mRequest)); //下载结束发一次广播
                    fileOut.close();
                    mResponse = downloadFile.getAbsolutePath().getBytes();
                } else {
                    while ((len = in.read(data)) != -1 && !Thread.currentThread().isInterrupted())
                        baos.write(data, 0, len);
                    Rx += baos.size();
                    mResponse = baos.toByteArray();
                }
                baos.close();
                in.close();
            }
            List<String> trustHost = NetManager.getInstance().getConfig().getTrustHost(); //调整信任服务器时间差
            if (trustHost != null) {
                for (String host : trustHost) {
                    if (url.getHost().equals(host)) {
                        mDiffServerTime = connection.getDate() - System.currentTimeMillis();
                        break;
                    }
                }
            }
            connection.disconnect();
            mRequest.setLastModified(connection.getHeaderField("Last-Modified"));
            mMessage = connection.getResponseMessage();
            saveExtraToRequest(connection);
            saveResponseStatus(connection.getResponseCode(), System.currentTimeMillis() - connectionTimer, connection.getResponseMessage());
            toggleCallback();
        } catch (IOException e){
            mException=e;
            e.printStackTrace();
            isSuccess = false;
            mMessage = e.toString();
            saveErrorNetStatus(e.getMessage(), connectionTimer);
            toggleCallback();
        } finally {
            if (mListener != null)
                mListener.onComplete(this);
        }
    }

    /**
     * 触发回调，理论上必定触发的回调
     */
    private void toggleCallback() {
        final GlobalListener globalListener = (NetManager.getInstance().getGlobalListener() == null ||
                !mRequest.isAcceptGlobalCallback() ? new GlobalListener() : NetManager.getInstance().getGlobalListener()); //如果这次请求不允许全局回调或者全局回调为空，返回空白全局回调，简化if判断
        final Listener l = mRequest.getListener();
        if (l == null || Thread.currentThread().isInterrupted())
            return;
        Object host = mRequest.getHost();
        boolean hostAvailable = true; //宿主是否状态正常.需要request里有宿主引用.如果没有宿主默认为在安全环境
        if (host instanceof Fragment) {
            Fragment fragment = (Fragment) host;
            if ((fragment.isRemoving() || fragment.isDetached()))
                hostAvailable = false;
        } else if (host instanceof Activity) {
            Activity activity = (Activity) host;
            if (activity.isFinishing())
                hostAvailable = false;
        }
        if (hostAvailable) {
            mResponse = globalListener.onRawData(mRequest, mResponse);
            l.onRawData(mRequest, mResponse);
            Type[] realType = mRequest.getGenericType();
            int realTypeIndex = 1;
            String json = null;

            try {
                Object responseObj;
                if (entityIsRawType(realType))
                    responseObj = mResponse;
                else if (entityIsStringType(realType)) {
                    responseObj = mResponse == null ? "" : new String(mResponse);
                    responseObj = globalListener.onTranslateJson(mRequest, (String) responseObj);
                    l.onTranslateJson(mRequest, (String) responseObj);
                } else {
                    json = mResponse == null ? "" : new String(mResponse);
                    json = globalListener.onTranslateJson(mRequest, json);
                    l.onTranslateJson(mRequest, json);
                    Pair<Integer, Object> pair = mResponse == null ? null : guessJsonEntity(json, realType);
                    if (pair != null) {
                        realTypeIndex = pair.first;
                        responseObj = pair.second;
                    } else
                        responseObj = null;
                }
                final int fRealTypeIndex = realTypeIndex;
                final Object fResponseObj = responseObj;
                if (isSuccess) {
                    mResponsePoster.execute(new Runnable() {
                        @Override
                        public void run() {
                            switch (fRealTypeIndex) {
                                case 1:
                                    Object cookedData = globalListener.onResponseListener(mRequest, fResponseObj, null);
                                    l.onResponseListener(mRequest, fResponseObj, null, cookedData);
                                    break;
                                case 2:
                                    l.onResponseListener(mRequest, null, fResponseObj, globalListener.onResponseListener(mRequest, null, fResponseObj));
                                    break;
                            }
                        }
                    });
                }
            } catch (JsonParseException e) {
                mMessage = "请求:" + mRequest + "\n解析时出现异常:" + e.getMessage() + "\njson字符串:" + json;
                isSuccess = false;
            }
            if (!isSuccess) {
                mResponsePoster.execute(new Runnable() {
                    @Override
                    public void run() {
                        mMessage = globalListener.onErrorListener(mRequest, mMessage);
                        l.onErrorListener(mRequest, mMessage);
                    }
                });
            }
        }
    }

    /**
     * 保存异常状态到Request
     * @param message     异常短信息
     * @param requestTime 开始请求时间
     */
    private void saveErrorNetStatus(String message, long requestTime) {
        mRequest.getResponseStatus().message = message;
        mRequest.getResponseStatus().time = 0;
        mRequest.getResponseStatus().code = -1;
        mRequest.getResponseStatus().time = System.currentTimeMillis() - requestTime;
    }

    /**
     * 检测错误流
     *
     * @param connection
     * @param requestTime
     * @throws IOException
     */
    private void checkErrorStream(HttpURLConnection connection, long requestTime) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            byte[] errbyte = new byte[4096];
            int len;
            while ((len = connection.getErrorStream().read(errbyte)) != -1)
                baos.write(errbyte, 0, len);
            ResponseStatus status = new ResponseStatus();
            status.message = connection.getResponseMessage();
            status.code = connection.getResponseCode();
            status.time = System.currentTimeMillis() - requestTime;
            mRequest.setResponseStatus(status);
            throw new NetException(baos.toString());
        }
    }

    /**
     * 从Request中取出并组合要发送的Cookie
     *
     * @return 组合好的Cookies
     */
    private String generateSendCookies() {
        List<Pair<String, String>> cookies = mRequest.getSendCookies();
        StringBuilder sb = new StringBuilder();
        if (cookies != null && !cookies.isEmpty())
            for (Pair<String, String> cookie : cookies) {
                sb.append(cookie.first)
                        .append("=")
                        .append(cookie.second)
                        .append(";");
            }
        return sb.toString();
    }

    /**
     * 保存一些诸如Header和Cookies的数据到Request中
     *
     * @param connection
     */
    private void saveExtraToRequest(HttpURLConnection connection) {
        Map<String, List<String>> map = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet())
            map.put(entry.getKey(), entry.getValue());
        mRequest.setReceiveHeader(map);
        Map<String, List<String>> cookiesMap = mRequest.getReceiveHeader();
        List<String> cookies = cookiesMap.remove("Set-Cookie");
        if (cookies != null && !cookies.isEmpty()) {
            Pair<String, String>[] cookieArray = new Pair[cookies.size()];
            for (int i = 0; i < cookies.size(); i++) {
                String cookie = cookies.get(i);
                cookieArray[i] = Pair.create(cookie.substring(0, cookie.indexOf('=')), cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';')));
            }
            mRequest.setReceiveCookies(cookieArray);
        }
    }

    /**
     * 保存返回状态值
     *
     * @param code
     * @param time
     * @param message
     */
    private void saveResponseStatus(int code, long time, String message) {
        ResponseStatus rs = new ResponseStatus();
        rs.code = code;
        rs.message = message;
        rs.time = time;
        mRequest.setResponseStatus(rs);
    }

    /**
     * 如果文件大小为0，并且不为Gzip流，不输出到文件中
     *
     * @param connection
     * @return
     */
    private boolean canWriteToFile(HttpURLConnection connection) {
        return mRequest.getDownloadable() != null
                && ((!TextUtils.isEmpty(connection.getHeaderField("Content-Length"))
                && connection.getHeaderFieldInt("Content-Length", 0) > 0)
                || mRequest.isReceiveGzip());
    }

    /**
     * 实体类型是否源类型
     *
     * @param types 实体类型组
     * @return 如果是源类型返回true，否则false
     */
    private boolean entityIsRawType(Type[] types) {
        if (types == null || types.length == 0) return true;
        for (Type type : types)
            if (type != null && type != Object.class && type != byte[].class)
                return false;
        return true;
    }

    /**
     * 实体类型是否字符串类型
     *
     * @param types 实体类型组
     * @return 如果是字符串类型返回true，否则false
     */
    private boolean entityIsStringType(Type[] types) {
        boolean hadStrType = false;
        boolean hadOtherType = false; //有除Object和String之外的类型
        for (Type type : types)
            if (type == String.class)
                hadStrType = true;
            else if (type != null && type != Object.class)
                hadOtherType = true;
        return hadStrType && !hadOtherType;
    }

    /**
     * json实体猜想
     *
     * @param json json字符串
     * @return 猜想成功返回成功参数的索引值和实体，否则为null
     */
    private Pair<Integer, Object> guessJsonEntity(String json, Type[] types) throws JsonParseException {
        Gson gson = new Gson();

        try {
            return Pair.create(1, gson.fromJson(json, types[0])); //第一次猜想
        } catch (JsonParseException e) {
            if (types.length > 1 && types[1] != null)
                return Pair.create(2, gson.fromJson(json, types[1])); //如果存在，进行第二次猜想
            throw e;
        }
    }

    /**
     * 拼接get方法的url
     *
     * @return
     */
    private String splicingGetUrl() {
        StringBuilder sb = new StringBuilder(mRequest.getUrl());
        Iterator<Pair<String, String>> iter = mRequest.getParamsRaw().iterator();

        if (iter.hasNext()) {
            Pair<String, String> pair = iter.next();
            sb.append("?").append(pair.first).append("=").append(pair.second);
        }
        while (iter.hasNext()) {
            Pair<String, String> pair = iter.next();
            sb.append("&").append(pair.first).append("=").append(pair.second);
        }
        return mRequest.isReplaceChinese() ? transferSpaceAndChinese(sb.toString()) : sb.toString();
    }

    /**
     * 拼接字符串参数
     *
     * @param params
     * @param sb
     */
    private void loadParams(List<Pair<String, String>> params, StringBuilder sb) {
        if (params == null || params.size() <= 0)
            return;
        Iterator<Pair<String, String>> iter = params.iterator();

        while (iter.hasNext()) {
            Pair<String, String> pair = iter.next();
            sb.append(pair.first).append("=").append(pair.second).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
    }

    /**
     * 混合数据发送
     *
     * @param strParams
     * @param fileParams
     * @param out
     * @throws IOException
     */
    private void multipart(List<Pair<String, String>> strParams, List<Pair<String, File>> fileParams, OutputStream out) throws IOException {
        if (strParams != null && strParams.size() > 0) {
            Iterator<Pair<String, String>> iter = strParams.iterator();
            StringBuilder sb = new StringBuilder();
            while (iter.hasNext() && !Thread.currentThread().isInterrupted()) {
                Pair<String, String> pair = iter.next();
                sb.append("--").append(BOUNDARY).append(CRLF)
                        .append("Content-Disposition:form-data; name=\"" + pair.first + "\"").append(CRLF)
                        .append("Content-Type:text/plain;charset=utf-8").append(CRLF + CRLF)
                        .append(pair.second).append(CRLF);
                Tx += sb.toString().getBytes().length;
                out.write(sb.toString().getBytes());
            }
        }

        if (fileParams != null && fileParams.size() > 0) {
            Iterator<Pair<String, File>> iter = fileParams.iterator();
            while (iter.hasNext() && !Thread.currentThread().isInterrupted()) {
                StringBuilder sb = new StringBuilder();
                Pair<String, File> pair = iter.next();
                if (pair.second != null && pair.second.exists() && pair.second.isFile()) {
                    sb.append("--" + BOUNDARY).append(CRLF)
                            .append("Content-Disposition:form-data; name=\"" + pair.first + "\";filename=\"" + pair.second.getName() + "\"").append(CRLF)
                            .append("Content-type: " + URLConnection.guessContentTypeFromName(pair.second.getName())).append(CRLF)
                            .append("Content-Transfer-Encoding:binary").append(CRLF + CRLF);
                    out.write(sb.toString().getBytes());
                    Tx += sb.toString().getBytes().length;
                    copyFileToStream(pair.second, out);
                    out.write(CRLF.getBytes());
                }
            }
        }
        out.write(END.getBytes());
        out.flush();
    }

    /**
     * 复制文件到输出流
     *
     * @param file
     * @param out
     * @throws IOException
     */
    private void copyFileToStream(File file, OutputStream out) throws IOException {
        if (file == null || !file.exists())
            return;
        OutputStream outDelegate = mRequest.isSendGzip() ? new GZIPOutputStream(out) : out;
        InputStream fileIn = new FileInputStream(file);
        byte[] data = new byte[BUFF_LENGTH];
        int len;
        long time = System.currentTimeMillis();
        long count = 0;
        int speed = 0;

        Context context = getHostContext();
        while ((len = fileIn.read(data)) != -1 && !Thread.currentThread().isInterrupted()) {
            outDelegate.write(data, 0, len);
            count += len;
            speed += len;
            Tx += len;
            if (context != null && (System.currentTimeMillis() - time) > 1000) {
                EventObserver.getInstance().sendEvent(context, new EventUploading(speed, count, file.getAbsolutePath()));
                speed = 0;
                time = System.currentTimeMillis();
            }
        }
        if (mRequest.isSendGzip())
            ((GZIPOutputStream) outDelegate).finish();
        fileIn.close();
    }

    /**
     * 空格和汉字替换成unicode
     *
     * @param str
     * @return
     */
    private String transferSpaceAndChinese(String str) {
        if (TextUtils.isEmpty(str))
            return "";
        StringBuilder sb = new StringBuilder(str);

        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fa5') {
                try {
                    sb.deleteCharAt(i);
                    sb.insert(i, URLEncoder.encode(String.valueOf(c), "UTF-8").toCharArray());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString().replace(" ", "%20"); //最后空格置换
    }

    public Context getHostContext() {
        Object host = mRequest.getHost();
        if (host instanceof Activity) return (Context) host;
        else if (host instanceof Fragment) return ((Fragment) host).getContext();
        return null;
    }

    @Override
    public String toString() {
        return "message:" + mMessage + " tx:" + Tx + " rx:" + Rx;
    }

    public long getTx() {
        return Tx;
    }

    public long getRx() {
        return Rx;
    }

    public String getMessage() {
        return mMessage;
    }

    public Request getRequest() {
        return mRequest;
    }

    public interface OnCompleteListener {
        void onComplete(NetProcessor processer);
    }

    public byte[] getResponse()throws IOException{
        if(mException!=null) throw mException;
        return mResponse;
    }
}