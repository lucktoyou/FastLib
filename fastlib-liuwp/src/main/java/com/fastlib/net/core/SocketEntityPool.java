package com.fastlib.net.core;

import com.fastlib.net.tool.URLUtil;

import java.io.IOException;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by sgfb on 2019/12/9
 * Modified by liuwp on 2020/9/30.
 * Socket留存池.当双方Header中没有指明Connection为close并且连接还未断开可重复使用连接的流
 */
public class SocketEntityPool{
    private static SocketEntityPool mInstance;
    private Map<String,List<SocketEntity>> mEntityMap=new HashMap<>();

    private SocketEntityPool(){}

    public static SocketEntityPool getInstance(){
        if(mInstance==null){
            synchronized(SocketEntityPool.class){
                mInstance=new SocketEntityPool();
            }
        }
        return mInstance;
    }

    /**
     * TODO 这里的同步是否可以优化
     * 从池中取一个socket实体(可能是新创建的)
     * @param url   统一资源定位符
     * @return      socket实体
     */
    public synchronized SocketEntity getSocketEntity(String url, Proxy proxy) throws IOException {
        String key= genEntityKey(URLUtil.getHostAndPort(url),proxy);
        List<SocketEntity> list=mEntityMap.get(key);

        if(list==null){
            list=new ArrayList<>();
            mEntityMap.put(key,list);
        }

        while(!list.isEmpty()){
            SocketEntity entity=list.remove(0);
            if(!sameProxy(entity.getProxy(),proxy)){
                list.add(entity);
            }
            if(entity.isValid()) return entity;
            else entity.close();
        }
        return new SocketEntity(url,!url.startsWith("https") ? new Socket(): SSLSocketFactory.getDefault().createSocket(),proxy);
    }

    private boolean sameProxy(Proxy p1,Proxy p2){
        if(p1!=null) return p1.equals(p2);
        else return p2==null;
    }

    /**
     * 返回一个实体到池中
     * @param entity    socket实体
     */
    public void returnSocketEntity(SocketEntity entity){
        String key=genEntityKey(URLUtil.getHostAndPort(entity.getUrl()),entity.getProxy());
        List<SocketEntity> list=mEntityMap.get(key);

        if(list==null){
            list=new ArrayList<>();
            mEntityMap.put(key,list);
        }
        list.add(entity);
    }

    private String genEntityKey(String url, Proxy proxy){
        String proxyStr=proxy==null?"":proxy.toString();
        return url+proxyStr;
    }
}