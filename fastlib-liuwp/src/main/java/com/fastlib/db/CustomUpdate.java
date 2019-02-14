package com.fastlib.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by sgfb on 16/5/16.
 * 数据库自定义升级方案
 */
public interface CustomUpdate{

    void update(SQLiteDatabase db,int oldVersion,int newVersion);
}