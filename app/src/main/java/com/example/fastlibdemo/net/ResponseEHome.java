package com.example.fastlibdemo.net;

public class ResponseEHome<T> {
    public boolean success;
    public String msg;
    public T obj;
    public String serviceName;

    @Override
    public String toString() {
        return "CommonBean{" +
                "success=" + success +
                ", msg='" + msg + '\'' +
                ", obj=" + obj +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
