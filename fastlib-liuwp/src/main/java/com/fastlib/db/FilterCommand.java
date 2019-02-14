package com.fastlib.db;

/**
 * Created by sgfb on 17/1/6.
 * 过滤命令
 */
public abstract class FilterCommand{
    public final static int TYPE_AND=1;
    public final static int TYPE_OR=2;
    protected Condition mCondition;
    protected FilterCommand mNext=null;
    protected FilterCommand mLast=null;

    public abstract int getType();

    public FilterCommand(Condition condition){
        mCondition=condition;
    }

    public FilterCommand and(Condition condition){
        FilterCommand fc=And.condition(condition);
        if(mNext==null){
            mNext=fc;
            mLast=fc;
        }
        else{
            mLast.mNext=fc;
            mLast=fc;
        }
        return this;
    }

    public FilterCommand or(Condition condition){
        FilterCommand fc=Or.condition(condition);
        if(mNext==null){
            mNext=fc;
            mLast=fc;
        }
        else{
            mLast.mNext=fc;
            mLast=fc;
        }
        return this;
    }

    public FilterCommand concat(FilterCommand filterCommand){
        if(mNext==null){
            mNext=filterCommand;
            mLast=filterCommand;
        }
        else{
            mLast.mNext=filterCommand;
            mLast=filterCommand;
        }
        return this;
    }

    public Condition getFilterCondition(){
        return mCondition;
    }

    public FilterCommand getNext(){
        return mNext;
    }
}