package com.fastlib.db;

/**
 * Created by sgfb on 16/3/21.
 * Modified by liuwp on 2021/6/23.
 * 数据库运行时一些参数.
 */
public final class RuntimeAttribute{
    private boolean mOrderAsc;//排序升序
    private String mOrderColumn; //根据这个字段排序
    private String mWhichDatabase;//仅本次操作,保存数据到指定数据库.如果这个数据库不存在,尝试创建数据库，如果异常这条语句将被丢弃不会抛出异常
    private int mSaveMax;//最大保存数,如果超出了这个值删除历史直到符合这个值
    private int mStart, mSize;//合起来就是limit
    private String[] mSelectColumn;
    private String[] mUnselectColumn;//如果这个字段为空取再判断selectColumn，如果selectColumn也是空，取所有列。如果这个字段不为空将不使用selectColumn字段
    private FilterCommand mFilterCommand;//过滤命令
    private Object[] mConstructorParams;//使用自定义构造对象时定义.如果元素为DataFromDatabase尝试从数据库中对应域获取值并且替换

    public RuntimeAttribute(){
        setDefaultAttribute();
    }

    public void setDefaultAttribute(){
        mOrderAsc = true;
        mOrderColumn = null;
        //mWhichDatabase不需要置空.
        mSaveMax = Integer.MAX_VALUE;
        mStart = 0;
        mSize = Integer.MAX_VALUE;
        mSelectColumn = null;
        mUnselectColumn = null;
        mFilterCommand = null;
        mConstructorParams = null;
    }

    /**
     * 是否正序
     * @param asc
     * @return
     */
    public RuntimeAttribute setOrderAsc(boolean asc){
        this.mOrderAsc = asc;
        return this;
    }

    public boolean getOrderAsc(){
        return mOrderAsc;
    }

    /**
     * 根据某个字段排序
     * @param column
     * @return
     */
    public RuntimeAttribute setOrderColumn(String column){
        this.mOrderColumn = column;
        return this;
    }

    public String getOrderColumn(){
        return mOrderColumn;
    }

    /**
     * 保存最大数(暂不使用)
     * @param max
     * @return
     */
    public RuntimeAttribute setSaveMax(int max){
        mSaveMax = max;
        return this;
    }

    public int getSaveMax(){
        return mSaveMax;
    }

    /**
     * 读取某一段数据
     * @param start
     * @param size
     * @return
     */
    public RuntimeAttribute limit(int start,int size){
        this.mStart = start;
        this.mSize = size;
        return this;
    }

    public int getStart(){
        return mStart;
    }

    public int getEnd(){
        return mSize;
    }

    /**
     * 单次对某个数据库进行读写
     * @param databaseName
     * @return
     */
    public RuntimeAttribute setToWhichDatabase(String databaseName){
        mWhichDatabase = databaseName;
        return this;
    }

    /**
     * 获取当前操作数据库名，加上.db后缀
     * @return 当前操作数据库名
     */
    public String getWhichDatabaseNameComplete(){
        return mWhichDatabase+".db";
    }

    /**
     * 获取当前操作数据库名
     * @return
     */
    public String getWhichDatabaseName(){
        return mWhichDatabase;
    }

    /**
     * 过滤出需要的字段.可以提高效率
     * @param selectColumn
     * @return
     */
    public RuntimeAttribute setSelectColumn(String[] selectColumn){
        mSelectColumn = selectColumn;
        return this;
    }

    public String[] getSelectColumn(){
        return mSelectColumn;
    }

    /**
     * 过滤掉不需要的字段,可以提高效率
     * @param unselectColumn
     * @return
     */
    public RuntimeAttribute setUnselectColumn(String[] unselectColumn){
        mUnselectColumn = unselectColumn;
        return this;
    }

    public String[] getUnselectColumn(){
        return mUnselectColumn;
    }

    /**
     * 过滤命令
     * @param filterCommand
     */
    public void setFilterCommand(FilterCommand filterCommand){
        mFilterCommand = filterCommand;
    }

    public void addFilterCommand(FilterCommand filterCommand){
        if(mFilterCommand==null)
            mFilterCommand = filterCommand;
        else
            mFilterCommand.concat(filterCommand);
    }

    public FilterCommand getFilterCommand(){
        return mFilterCommand;
    }

    /**
     * 非空构造给予参数.
     * @param constructorParams
     * @return
     */
    public RuntimeAttribute setConstructorParams(Object[] constructorParams){
        mConstructorParams = constructorParams;
        return this;
    }

    public Object[] getConstructorParams(){
        return mConstructorParams;
    }
}