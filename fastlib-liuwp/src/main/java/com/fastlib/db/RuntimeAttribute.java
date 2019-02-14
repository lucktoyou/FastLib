package com.fastlib.db;

/**
 * Created by sgfb on 16/3/21.
 * 数据库运行时一些参数
 */
public final class RuntimeAttribute{
    private boolean mAsc;
    private int mSaveMax; //最大保存数,如果超出了这个值删除历史直到符合这个值
    private int mStart,mSize; //合起来就是limit
    private String mOrderBy;
    private String mWhichDatabase; //仅本次操作,保存数据到指定数据库.如果这个数据库不存在,尝试创建数据库，如果异常这条语句将被丢弃不会抛出异常
    private String[] mSelectColumn;
    private String[] mUnselectColumn; //如果这个字段为空取再判断selectColumn，如果selectColumn也是空，取所有列。如果这个字段不为空将不使用selectColumn字段
    private FilterCommand mFilterCommand;
    private Object[] mConstructorParams; //使用自定义构造对象时定义.如果元素为DataFromDatabase尝试从数据库中对应域获取值并且替换

    public RuntimeAttribute(){
        defaultAttribute();
    }

    public void defaultAttribute(){
        mAsc =true;
        mOrderBy =null;
        mWhichDatabase =null;
        mSaveMax =Integer.MAX_VALUE;
        mStart =0;
        mSize =Integer.MAX_VALUE;
    }

    /**
     * 是否正序
     * @param asc
     * @return
     */
    public RuntimeAttribute setOrderAsc(boolean asc){
        this.mAsc =asc;
        return this;
    }

    /**
     * 保存最大数(暂不使用)
     * @param max
     * @return
     */
    public RuntimeAttribute setSaveMax(int max){
        mSaveMax =max;
        return this;
    }

    /**
     * 读取某一段数据
     * @param start
     * @param size
     * @return
     */
    public RuntimeAttribute limit(int start,int size){
        this.mStart =start;
        this.mSize =size;
        return this;
    }

    /**
     * 根据什么字段排序
     * @param orderBy
     * @return
     */
    public RuntimeAttribute orderBy(String orderBy){
        this.mOrderBy =orderBy;
        return this;
    }

    /**
     * 单次对某个数据库进行读写
     * @param toWhichDatabase
     * @return
     */
    public RuntimeAttribute setToWhichDatabase(String toWhichDatabase){
        mWhichDatabase=toWhichDatabase;
        return this;
    }

    /**
     * 过滤出需要的字段.可以提高效率
     * @param selectColumn
     * @return
     */
    public RuntimeAttribute setSelectColumn(String[] selectColumn){
        mSelectColumn=selectColumn;
        return this;
    }

    /**
     * 过滤掉不需要的字段,可以提高效率
     * @param unselectColumn
     * @return
     */
    public RuntimeAttribute setUnselectColumn(String[] unselectColumn){
        mUnselectColumn=unselectColumn;
        return this;
    }

    public boolean isAsc() {
        return mAsc;
    }

    public int getSaveMax() {
        return mSaveMax;
    }

    public int getStart() {
        return mStart;
    }

    public int getEnd() {
        return mSize;
    }

    public String getOrderBy() {
        return mOrderBy;
    }

    /**
     * 获取当前操作数据库名，加上.db后缀
     * @return 当前操作数据库名
     */
    public String getWhichDatabaseComplete() {
        return mWhichDatabase+".db";
    }

    /**
     * 获取当前操作数据库名
     * @return
     */
    public String getWhichDatabase(){
        return mWhichDatabase;
    }

    public String[] getSelectColumn() {
        return mSelectColumn;
    }

    public String[] getUnselectColumn() {
        return mUnselectColumn;
    }

    public FilterCommand getFilterCommand() {
        return mFilterCommand;
    }

    public void setFilterCommand(FilterCommand filterCommand) {
        mFilterCommand = filterCommand;
    }

    public void addFilterCommand(FilterCommand filterCommand){
        if(mFilterCommand==null)
            mFilterCommand=filterCommand;
        else
            mFilterCommand.concat(filterCommand);
    }

    public Object[] getConstructorParams() {
        return mConstructorParams;
    }

    public RuntimeAttribute setConstructorParams(Object[] constructorParams) {
        mConstructorParams = constructorParams;
        return this;
    }
}