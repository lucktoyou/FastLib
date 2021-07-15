package com.fastlib.net.exception;

/**
 * Created by sgfb on 2020\03\13.
 * 手动取消
 */
public class CancelException extends NetException {

    public CancelException() {
        super("手动取消");
    }
}
