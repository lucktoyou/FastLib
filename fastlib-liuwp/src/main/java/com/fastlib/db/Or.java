package com.fastlib.db;

/**
 * Created by sgfb on 17/1/6.
 * 数据库或条件过滤
 */
public class Or extends FilterCommand{

    private Or(Condition condition) {
        super(condition);
    }

    public static Or condition(Condition condition){
        return new Or(condition);
    }

    @Override
    public int getType() {
        return TYPE_OR;
    }
}