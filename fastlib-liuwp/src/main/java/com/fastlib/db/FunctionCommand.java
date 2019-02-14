package com.fastlib.db;

import android.support.annotation.Nullable;

/**
 * Created by sgfb on 17/7/9.
 * 数据库函数命令.指定属性值为某过滤段的指定函数返回
 */
public class FunctionCommand{
    private FunctionType mType;
    private FilterCommand mFilterCommand;

    private FunctionCommand( FunctionType type){
        mType = type;
    }

    private FunctionCommand(FunctionType type, @Nullable FilterCommand filterCommand) {
        mType = type;
        mFilterCommand = filterCommand;
    }

    /**
     * 返回字段叠加数
     * @return 叠加函数命令
     */
    public static FunctionCommand sum(){
        return new FunctionCommand(FunctionType.SUM);
    }

    /**
     * 返回字段叠加数，有过滤条件
     * @param filterCommand 过滤条件
     * @return 叠加函数命令
     */
    public static FunctionCommand sum(FilterCommand filterCommand){
        return new FunctionCommand(FunctionType.SUM,filterCommand);
    }

    /**
     * 返回值最大字段
     * @return 最大字段过滤函数命令
     */
    public static FunctionCommand max(){
        return new FunctionCommand(FunctionType.MAX);
    }

    /**
     * 返回值最大字段，有过滤条件
     * @param filterCommand 过滤条件
     * @return 最大字段过滤函数命令
     */
    public static FunctionCommand max(FilterCommand filterCommand){
        return new FunctionCommand(FunctionType.MAX,filterCommand);
    }

    /**
     * 返回值最小字段
     * @return 最小字段过滤函数命令
     */
    public static FunctionCommand min(){
        return new FunctionCommand(FunctionType.MIN);
    }

    /**
     * 返回值最小字段，有过滤条件
     * @param filterCommand 过滤条件
     * @return 最小字段过滤函数命令
     */
    public static FunctionCommand min(FilterCommand filterCommand){
        return new FunctionCommand(FunctionType.MIN,filterCommand);
    }

    /**
     * 返回所有字段平均值
     * @return 平均函数命令
     */
    public static FunctionCommand avg(){
        return new FunctionCommand(FunctionType.AVG);
    }

    /**
     * 返回所有字段平均值，有过滤条件
     * @param filterCommand 过滤条件
     * @return 平均函数命令
     */
    public static FunctionCommand avg(FilterCommand filterCommand){
        return new FunctionCommand(FunctionType.AVG,filterCommand);
    }

    public FunctionType getType() {
        return mType;
    }

    public FilterCommand getFilterCommand() {
        return mFilterCommand;
    }
}