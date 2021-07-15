package com.fastlib.utils.zipimage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Modified by liuwp 2020/11/23.
 * 当打开一个新的InputStream时，自动关闭之前的InputStream，最后需要手动调用{@link #close()}来释放资源。
 */
public abstract class InputStreamAdapter implements InputStreamProvider {

    private InputStream inputStream;

    @Override
    public InputStream open() throws IOException {
        close();
        inputStream = openInternal();
        return inputStream;
    }

    public abstract InputStream openInternal() throws IOException;

    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignore) {
            } finally {
                inputStream = null;
            }
        }
    }
}