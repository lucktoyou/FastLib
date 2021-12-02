package com.fastlib.net;

import java.io.IOException;

/**
 * Created by liuwp on 2021\11\30.
 */
public class CustomException extends IOException {

    public CustomException(String message){
        super(message);
    }
}
