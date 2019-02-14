package com.fastlib.net;

import java.io.IOException;

/**
 * Created by sgfb on 17/7/31.
 */
public class NetException extends IOException{
    public NetException() {
        super();
    }

    public NetException(String detailMessage) {
        super(detailMessage);
    }

    public NetException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetException(Throwable cause) {
        super(cause);
    }
}
