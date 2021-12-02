package com.fastlib.net.core;

import android.text.TextUtils;

import com.fastlib.net.tool.URLUtil;
import com.fastlib.utils.FastLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by sgfb on 2019/12/3
 * E-mail:602687446@qq.com
 * 管理Socket.支持最低程度Http协议连接,例如控制输入输出顺序,处理(仅应该处理且能处理)的一些事务
 */
public abstract class HttpCore {
    private static final boolean DEBUG = false;
    private static final String HTTP_PROTOCOL = "HTTP/1.1";
    private static final String CRLF = "\r\n";

    private SocketEntity mSocketEntity;
    private Proxy mProxy;

    protected boolean isBegin = false;
    protected String mUrl;
    protected RequestHeader mRequestHeader;
    protected ResponseHeader mResponseHeader;
    protected HttpTimer mTimer;
    protected int mSendHeaderLength;
    protected int mReceivedHeaderLength;

    /**
     * 生成必要的和自定义的头部
     *
     * @return 请求的HTTP头部
     */
    protected abstract Map<String, List<String>> getHeader() throws IOException;

    /**
     * 发送请求体给服务器
     */
    protected abstract void onSendData() throws IOException;

    /**
     * 获取请求方法
     *
     * @return 请求方法(GET, POST等)
     */
    protected abstract String getRequestMethod();

    /**
     * 返回一个配置单
     *
     * @return 配置单
     */
    protected abstract HttpOption getHttpOption();

    public HttpCore(String url) {
       init(url);
    }

    private void init(String url){
        if (!URLUtil.validUrl(url))
            throw new IllegalArgumentException("url不正确或者不支持的协议:" + url);
        mUrl = url;
        mTimer = new HttpTimer();
    }

    public void begin() throws IOException {
        mTimer.nextProcess();
        isBegin = true;
        Socket socket = null;
        List<Proxy> proxyList = new ArrayList<>(ProxySelector.getDefault().select(URI.create(mUrl)));
        //最后添加一个null proxy以当做无代理连接
        proxyList.add(null);
        for (Proxy proxy : proxyList) {
            try {
                mProxy = proxy;

                if (proxy != null && proxy.type() == Proxy.Type.DIRECT) continue;
                mSocketEntity = SocketEntityPool.getInstance().getSocketEntity(mUrl, proxy);
                socket = mSocketEntity.getSocket();
                if (!socket.isConnected()) {
                    String hostname = proxy != null && proxy.address() instanceof InetSocketAddress ? ((InetSocketAddress) proxy.address()).getHostName() : URLUtil.getHost(mUrl);
                    int port = proxy != null && proxy.address() instanceof InetSocketAddress ? ((InetSocketAddress) proxy.address()).getPort() : URLUtil.getPort(mUrl);
                    socket.connect(new InetSocketAddress(hostname, port), getHttpOption().connectionTimeout);
                    if(DEBUG) {
                        FastLog.d("Http已连接（新连接）");
                    }
                } else {
                    if(DEBUG) {
                        FastLog.d("Http已连接（复用连接）");
                    }
                }
            } catch (IOException exception) {
                //当使用无代理出现异常则抛出这个异常
                if (mProxy == null) throw exception;
            }
            if (socket != null && socket.isConnected()) break;
        }
        socket.setSoTimeout(getHttpOption().readTimeout);
        if(DEBUG){
            FastLog.d(String.format("socket isBound=%b isConnected=%b isOutputShutdown=%b isInputShutdown=%b isClosed=%b",
                    socket.isBound(),socket.isConnected(),socket.isOutputShutdown(),socket.isInputShutdown(),socket.isClosed()));
        }
        mTimer.nextProcess();
        sendHeader();
        onSendData();
        receiveHeader();
        handleResponseStatus();
    }

    /**
     * 发送头部
     */
    private void sendHeader() throws IOException {
        Map<String, List<String>> header = getHeader();
        StringBuilder sb = new StringBuilder(256);

        //例：GET /index HTTP/1.1
        String uri = mProxy == null ? URLUtil.getPath(mUrl) : mUrl;
        sb.append(getRequestMethod()).append(' ').append(uri).append(' ').append(HTTP_PROTOCOL).append(CRLF);
        for (Map.Entry<String, List<String>> entry : header.entrySet()) {
            for (String headerValue : entry.getValue()) {
                sb.append(entry.getKey()).append(':').append(headerValue).append(CRLF);
            }
        }
        sb.append(CRLF);

        byte[] headerByte = sb.toString().getBytes();
        mSendHeaderLength = headerByte.length;
        mRequestHeader = new RequestHeader(getRequestMethod(),uri,HTTP_PROTOCOL,header);
        mSocketEntity.getOutputStream().write(headerByte);
    }

    /**
     * 接收头部
     */
    private void receiveHeader() throws IOException {
        mSocketEntity.getOutputStream().flush();
        String statusLine = readLine(mSocketEntity.getInputStream());
        //如果第一次读取到空的状态行，再读取一次，在Transfer-Encoding为chunked类型时会发生多了个空行的问题
        if (TextUtils.isEmpty(statusLine)) statusLine = readLine(mSocketEntity.getInputStream());
        mTimer.nextProcess();
        if (TextUtils.isEmpty(statusLine)) throw new IOException("服务器返回HTTP协议异常");
        String[] status = statusLine.trim().split(" ");
        if (status.length < 2) throw new IOException("服务器返回HTTP协议异常");
        try {
            int code = Integer.parseInt(status[1].trim());
            Map<String, List<String>> header = new HashMap<>();
            String line;

            while (!TextUtils.isEmpty((line = readLine(mSocketEntity.getInputStream())))) {
                String key;
                List<String> value;
                int firstColonIndex = line.indexOf(':');
                if (firstColonIndex == -1) key = null;
                else key = line.substring(0, firstColonIndex).trim();

                value = header.get(key);
                if (value == null) {
                    value = new ArrayList<>();
                    header.put(key, value);
                }
                if (firstColonIndex < line.length())
                    value.add(firstColonIndex == -1 ? line.trim() : line.substring(firstColonIndex + 1).trim());
                else value.add("");
                header.put(key, value);
            }
            mResponseHeader = new ResponseHeader(code, status[0], status.length > 2 ? status[2] : "", header);
            if(DEBUG){
                FastLog.d(String.format(Locale.getDefault(), "code:%d message:%s", mResponseHeader.getCode(), mResponseHeader.getMessage()));
            }
        } catch (NumberFormatException e) {
            throw new IOException("服务器返回状态码异常,状态码:" + status[1]);
        }
    }

    /**
     * 检查并处理可处理项
     */
    private void handleResponseStatus() throws IOException {
        if (mResponseHeader == null || mResponseHeader.getCode() == 0) {
            throw new IOException("未能处理的返回协议");
        }
        int code = mResponseHeader.getCode();

        //重定位
        if ((code == ResponseCodeDefinition.MULTIPLE_CHOICES || code == ResponseCodeDefinition.MOVED_PERMANENTLY ||
                code == ResponseCodeDefinition.MOVE_TEMPORARILY || code == ResponseCodeDefinition.SEE_OTHER) && getHttpOption().autoRelocation) {
            String location = mResponseHeader.getHeaderFirst(HeaderDefinition.KEY_LOCATION);

            if (!TextUtils.isEmpty(location)) {
                end();
                if(DEBUG){
                    FastLog.d("重定向到:" + location);
                }
                init(location);
                begin();
            }
        }
    }

    protected String readLine(InputStream inputStream) throws IOException {
        int lastChar = 0;
        int currChar = 0;
        StringBuilder sb = new StringBuilder(100);

        while (lastChar != '\t' && currChar != '\n') {
            lastChar = currChar;
            currChar = inputStream.read();

            if (currChar == -1) break;
            sb.append((char) currChar);
        }

        mReceivedHeaderLength += sb.toString().getBytes().length;
        if (sb.length() >= 2 && sb.substring(sb.length() - 2).equals(CRLF))
            sb.delete(sb.length() - 2, sb.length());
        //TODO 是否读错了位置？
        if ("\n".equals(sb.toString()))
            return readLine(inputStream);
        return sb.toString();
    }

    protected OutputStream getSocketOutputStream() throws IOException {
        if (mSocketEntity == null) begin();
        return mSocketEntity.getOutputStream();
    }

    /**
     * 正常结束
     */
    public void end() throws IOException {
        if (mSocketEntity != null) {
            mTimer.nextProcess();
            if (isKeepAlive())
                SocketEntityPool.getInstance().returnSocketEntity(mSocketEntity);
            mSocketEntity = null;
            if(DEBUG){
                FastLog.d("Http请求结束");
            }
        }
    }

    /**
     * 丢弃(非正常结束,如手动取消)
     */
    public void discard(){
        if(mSocketEntity!=null){
            mTimer.nextProcess();
            try {
                mSocketEntity.close();
            } catch (IOException e) {
                //已被丢弃,不处理任何异常
            }
            mSocketEntity=null;
            if(DEBUG){
                FastLog.d("Http请求已丢弃");
            }
        }
    }

    public InputStream getInputStream() throws IOException {
        if (mSocketEntity == null) begin();
        return mSocketEntity.getInputStream();
    }

    public boolean isConnected() {
        return mSocketEntity != null;
    }

    public RequestHeader getRequestHeader() {
        return mRequestHeader;
    }

    public ResponseHeader getResponseHeader() {
        return mResponseHeader;
    }

    /**
     * 是否支持长连接优化
     *
     * @return true支持 false不支持
     */
    protected boolean isKeepAlive() throws IOException {
        if (mResponseHeader == null) return false;

        String serverConnection = mResponseHeader.getHeaderFirst(HeaderDefinition.KEY_CONNECTION);
        if (serverConnection == null || HeaderDefinition.VALUE_CONNECTION_CLOSE.equals(serverConnection)) return false;

        List<String> clientConnection = getHeader().get(HeaderDefinition.KEY_CONNECTION);
        return clientConnection == null || !clientConnection.contains("close");
    }

    public HttpTimer getHttpTimer() {
        return mTimer;
    }
}
