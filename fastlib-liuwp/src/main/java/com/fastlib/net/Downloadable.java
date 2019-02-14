package com.fastlib.net;

import java.io.File;

/**
 * Created by sgfb on 16/2/22.
 */
public interface Downloadable{

    /**
     * 获取下载目标文件位置
     * @return 目标文件
     */
    File getTargetFile();

    /**
     * 是否支持中断
     * @return 中断支持
     */
    boolean supportBreak();

    /**
     * 如果服务器给予了文件名,是否修改文件名
     * @return 是否支持
     */
    boolean changeIfHadName();

    /**
     * 标识文件过期与没过期是否下载的判断
     * @return 如果为空则不判断过期直接下载，否则过期才下载
     */
    String expireTime();
}
