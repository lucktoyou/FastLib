package com.fastlib.net.exception;

import java.io.IOException;

/**
 * Created by sgfb on 2020\03\09.
 */
public class NetException extends IOException {

    public NetException(String message){
        super(message);
    }
}
