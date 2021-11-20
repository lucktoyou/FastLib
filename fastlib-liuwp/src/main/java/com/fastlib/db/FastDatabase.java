package com.fastlib.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fastlib.annotation.Database;
import com.fastlib.annotation.DatabaseFileRef;
import com.fastlib.base.ThreadPoolManager;
import com.fastlib.bean.DatabaseTable;
import com.fastlib.utils.core.Reflect;
import com.fastlib.utils.core.SaveUtil;
import com.fastlib.utils.FastLog;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by sgfb on 17/7/10.
 * Modified by liuwp on 2021/6/23.
 * Modified by liuwp on 2021/11/19.
 * 封装一些与数据库交互的基本操作。非orm数据库。
 * 在调用次数少或者响应要求不高的的场景可以直接调用增删改，否则应该使用xxxAsync，获取回调数据使用
 */
public class FastDatabase{
    private static final DatabaseConfig sConfig = getConfig();
    private final Context mContext;
    private final RuntimeAttribute mAttribute;
    private final Map<String,FunctionCommand> mFunctionCommand; //列名-->功能函数
    private CustomUpdate mCustomUpdate;

    private FastDatabase(Context context){
        mContext = context.getApplicationContext();
        mAttribute = new RuntimeAttribute();
        mFunctionCommand = new HashMap<>();
    }

    /**
     * 返回公共数据库
     * @param context 上下文
     * @return 数据库
     */
    public static FastDatabase getDefaultInstance(Context context){
        return getInstance(context,sConfig.getDefaultDatabaseName());
    }

    /**
     * 返回指定数据库(仅单次有效并未转换数据库)
     * @param context 上下文
     * @param dbName 数据库名（将会主动添加.db后辍）
     * @return 数据库
     */
    public static FastDatabase getInstance(Context context,String dbName){
        FastDatabase database = new FastDatabase(context);
        database.toWhichDatabase(dbName);
        return database;
    }

    /**
     * 异步存或修改数据
     * @param obj 对象类
     * @param callback 结束后回调
     */
    public void saveOrUpdateAsync(final Object obj,final DatabaseNoDataResultCallback callback){
        ThreadPoolManager.sSlowPool.execute(new Runnable(){
            @Override
            public void run(){
                Handler handle = new Handler(Looper.getMainLooper());
                final boolean success = saveOrUpdate(obj);
                if(callback!=null)
                    handle.post(new Runnable(){
                        @Override
                        public void run(){
                            callback.onResult(success);
                        }
                    });
            }
        });
    }

    /**
     * 异步删除数据库中数据
     * @param cla 对象类
     * @param callback 结束后回调
     */
    public void deleteAsync(final Class<?> cla,final DatabaseNoDataResultCallback callback){
        ThreadPoolManager.sSlowPool.execute(new Runnable(){
            @Override
            public void run(){
                Handler handle = new Handler(Looper.getMainLooper());
                final boolean success = delete(cla);
                if(callback!=null)
                    handle.post(new Runnable(){
                        @Override
                        public void run(){
                            callback.onResult(success);
                        }
                    });
            }
        });
    }

    /**
     * 异步获取数据库请求第一条纪录
     * @param cla 对象类
     * @param listener 监听回调
     * @param <T> 任意泛型
     */
    public <T> void getFirstAsync(final Class<T> cla,final DatabaseGetCallback<T> listener){
        ThreadPoolManager.sSlowPool.execute(new Runnable(){
            @Override
            public void run(){
                Handler handle = new Handler(Looper.getMainLooper());
                final T data = getFirst(cla);
                handle.post(new Runnable(){
                    @Override
                    public void run(){
                        listener.onResult(data);
                    }
                });
            }
        });
    }

    /**
     * 获取数据库请求第一条记录
     * @param cla 对象类
     * @param <T> 任意泛型
     * @return 不为空则返回表中首个数据，否则返回null
     */
    public <T> T getFirst(Class<T> cla){
        List<T> all = limit(0,1).get(cla);
        if(all!=null && !all.isEmpty())
            return all.get(0);
        return null;
    }

    /**
     * 异步获取数据库记录
     * @param cla 对象类
     * @param listener 监听回调
     * @param <T> 任意泛型
     */
    public <T> void getAsync(final Class<T> cla,final DatabaseListGetCallback<T> listener){
        ThreadPoolManager.sSlowPool.execute(new Runnable(){
            @Override
            public void run(){
                Handler handler = new Handler(Looper.getMainLooper());
                final List<T> list = get(cla);
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        listener.onResult(list);
                    }
                });
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * 获取表中数据
     * @param cla 对象类
     * @return 根据过滤条件返回的数据（如果没有过滤条件返回指定表的所有数据）
     */
    public <T> List<T> get(Class<T> cla){
        SQLiteDatabase db = prepare(null);
        String databaseName = db.getPath().substring(db.getPath().lastIndexOf(File.separator)+1);
        String tableName = cla.getCanonicalName();
        if(!tableExists(db,tableName)){
            FastLog.d(databaseName+"不存在表"+tableName);
            db.close();
            return null;
        }
        Cursor cursor;
        String filters;
        String order;
        String limit = "";
        List<String> selectionArgs = new ArrayList<>();
        List<T> list = new ArrayList<>();
        String key = getPrimaryKeyName(cla);
        String selectColumn = getSelectColumn(cla);
        //过滤条件
        filters = getFilters(key,mAttribute.getFilterCommand(),selectionArgs);
        //排序条件
        if(!TextUtils.isEmpty(mAttribute.getOrderColumn())){
            //按指定列排序
            order = " order by "+mAttribute.getOrderColumn()+" "+(mAttribute.getOrderAsc() ? "asc" : "desc");
        }else {
            if(!TextUtils.isEmpty(key)){
                //按主键排序
                order = " order by "+key+" "+(mAttribute.getOrderAsc() ? "asc" : "desc");
            }else {
                order = "";//不排序
            }
        }
        //限制条件
        limit = " limit "+mAttribute.getStart()+","+mAttribute.getEnd();
        String complete = "select "+selectColumn+" from '"+tableName+"'"+filters+order+limit;
        String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[]{});
        cursor = db.rawQuery(complete,args);
        if(cursor==null){
            FastLog.d("请求的数据不存在数据库");
            db.close();
            return null;
        }
        Gson gson = new Gson();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            try{
                Object[] constructorParams = mAttribute.getConstructorParams();
                T obj = (constructorParams==null || constructorParams.length<=0) ? cla.newInstance() : null;
                Field[] fields = cla.getDeclaredFields();
                Map<String,Object> params = new HashMap<>();

                for(Field field: fields){
                    Database fieldInject = field.getAnnotation(Database.class);
                    DatabaseFileRef fileRef = field.getAnnotation(DatabaseFileRef.class);
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    String columnName = (fieldInject!=null && !TextUtils.isEmpty(fieldInject.columnName()))? fieldInject.columnName():field.getName();
                    int columnIndex = cursor.getColumnIndex(columnName);

                    if(columnIndex==-1)
                        continue;
                    if(fieldInject!=null && fieldInject.ignore()) //跳过忽视字段
                        continue;
                    //函数命令.-->
                    if(mFunctionCommand.containsKey(columnName)){
                        FunctionCommand functionCommand = mFunctionCommand.get(columnName);
                        boolean functionSuccess = false;
                        if(functionCommand!=null){
                            List<String> functionArgs = new ArrayList<>();
                            Cursor functionCursor = db.rawQuery("select "+functionCommand.getType().getName()+"("+columnName+")"+" from '"+tableName+
                                    "'"+getFilters(key,functionCommand.getFilterCommand(),functionArgs),functionArgs.toArray(new String[]{}));
                            if(functionCursor!=null){
                                functionCursor.moveToFirst();
                                if(type==short.class){
                                    field.setShort(obj,functionCursor.getShort(0));
                                    functionSuccess = true;
                                } else if(type==int.class){
                                    field.setInt(obj,functionCursor.getInt(0));
                                    functionSuccess = true;
                                }else if (type==long.class){
                                    field.setLong(obj,functionCursor.getLong(0));
                                    functionSuccess = true;
                                }else if (type==float.class){
                                    field.setFloat(obj,functionCursor.getFloat(0));
                                    functionSuccess = true;
                                }else if (type==double.class){
                                    field.setDouble(obj,functionCursor.getDouble(0));
                                    functionSuccess = true;
                                }else {
                                    FastLog.d("目前函数命令仅对数字类型的列有效");
                                }
                                functionCursor.close();
                            }
                        }
                        mFunctionCommand.remove(field.getName());//清除掉函数命令
                        if(functionSuccess) continue;
                    }
                    //<--
                    if(type==boolean.class){
                        int value = cursor.getInt(columnIndex);
                        if(obj!=null) field.setBoolean(obj,value>0);
                        else params.put(field.getName(),value>0);
                    }else if(type==int.class){
                        if(obj!=null) field.setInt(obj,cursor.getInt(columnIndex));
                        else params.put(field.getName(),cursor.getInt(columnIndex));
                    }else if(type==long.class){
                        if(obj!=null) field.setLong(obj,cursor.getLong(columnIndex));
                        else params.put(field.getName(),cursor.getLong(columnIndex));
                    }else if(type==float.class){
                        if(obj!=null) field.setFloat(obj,cursor.getFloat(columnIndex));
                        else params.put(field.getName(),cursor.getFloat(columnIndex));
                    }else if(type==double.class){
                        if(obj!=null) field.setDouble(obj,cursor.getDouble(columnIndex));
                        else params.put(field.getName(),cursor.getDouble(columnIndex));
                    }else if(type==short.class){
                        if(obj!=null) field.setShort(obj,cursor.getShort(columnIndex));
                        else params.put(field.getName(),cursor.getShort(columnIndex));
                    }else if(type==byte[].class){
                        if(obj!=null) field.set(obj,cursor.getBlob(columnIndex));
                        else params.put(field.getName(),cursor.getBlob(columnIndex));
                    }else if(type==String.class){
                        String value = cursor.getString(columnIndex);

                        if(fileRef!=null){
                            File file = new File(value);
                            value = new String(SaveUtil.loadFile(file.getAbsolutePath()));
                        }
                        if(obj!=null) field.set(obj,value);
                        else params.put(field.getName(),value);
                    }else{
                        String value = cursor.getString(columnIndex);
                        String json;
                        if(fileRef==null)
                            json = value;
                        else{
                            File file = new File(value);
                            json = new String(SaveUtil.loadFile(file.getAbsolutePath()));
                        }
                        Object preObj = gson.fromJson(json,field.getGenericType());
                        if(obj!=null) field.set(obj,preObj);
                        else params.put(field.getName(),preObj);
                    }
                }
                //如果是使用非空构造实例化对象的话，整理一下构造参数，实例化并且将参数注入.-->
                if(constructorParams!=null && constructorParams.length>0){
                    for(int i = 0;i<constructorParams.length;i++){
                        Object raw = constructorParams[i];
                        if(raw instanceof DataFromDatabase)
                            constructorParams[i] = params.remove(((DataFromDatabase)raw).getField());
                    }
                    Class<?> clas[] = new Class[constructorParams.length];
                    for(int i = 0;i<constructorParams.length;i++)
                        clas[i] = constructorParams[i].getClass();
                    Constructor<?>[] constructors = cla.getDeclaredConstructors();
                    //遍历构造函数，等长且除Object类型之外元素一致的情况下，保持Object一致
                    for(Constructor<?> constructor1: constructors){
                        Class<?>[] formalParams = constructor1.getParameterTypes();
                        if(clas.length!=formalParams.length)
                            continue;
                        for(int j = 0;j<formalParams.length;j++){
                            if(formalParams[j]!=Object.class && !Reflect.equalBasicOrBasicObj(formalParams[j],clas[j]))
                                continue;
                            if(formalParams[j]==Object.class)
                                clas[j] = Object.class;
                            else
                                clas[j] = formalParams[j];
                        }
                    }
                    Constructor<T> constructor = cla.getDeclaredConstructor(clas);
                    obj = constructor.newInstance(constructorParams);
                    //参数注入.这一块有优化可能
                    for(String fieldName: params.keySet()){
                        Field field = cla.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(obj,params.get(fieldName));
                    }
                }
                //<--
                list.add(obj);
                cursor.moveToNext();
            }catch(Exception e){
                FastLog.e("数据库在取数据时发生异常:"+e.toString());
                db.close();
                return null;
            }
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 删除对象(obj对象必需有主键)
     * @param obj 有主键的对象
     * @return 成功删除返回true，否则fase
     */
    public boolean delete(Object obj){
        Field[] fields = obj.getClass().getDeclaredFields();
        Field keyField = null;
        String keyFieldValue;

        //是否有主键
        for(Field field: fields){
            field.setAccessible(true);
            Database tableInject = field.getAnnotation(Database.class);
            if(tableInject!=null && tableInject.keyPrimary()){
                keyField = field;
                break;
            }
        }
        if(keyField ==null){
            FastLog.d("错误的使用了delete(Object obj),obj没有注解主键");
            return false;
        }else{
            String fieldType = getFieldTypeByConverted(keyField);
            if(!Reflect.isInteger(fieldType) && !Reflect.isReal(fieldType) && !Reflect.isVarchar(fieldType)){
                FastLog.d("不支持成为主键的字段类型："+fieldType);
                return false;
            }
            try{
                keyFieldValue = Reflect.objToStr(keyField.get(obj));
            }catch(IllegalAccessException e){
                FastLog.d(e.toString());
                return false;
            }
        }
        setFilter(And.condition(Condition.equal(keyFieldValue)));
        return delete(obj.getClass());
    }

    /**
     * 删除数据
     * @param cla 对象类
     * @return 成功删除返回true，否则为false
     */
    public boolean delete(Class<?> cla){
        SQLiteDatabase db = prepare(null);
        String databaseName = db.getPath().substring(db.getPath().lastIndexOf(File.separator)+1);
        String tableName = cla.getCanonicalName();
        if(!tableExists(db,tableName)){
            FastLog.d(databaseName+"不存在表"+tableName);
            db.close();
            return false;
        }
        Cursor cursor;
        String filters;
        String key = getPrimaryKeyName(cla);
        List<String> selectionArgs = new ArrayList<>();

        filters = getFilters(key,mAttribute.getFilterCommand(),selectionArgs);
        String complete = "select *from '"+tableName+"'"+filters;
        String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[]{});
        cursor = db.rawQuery(complete,args);
        cursor.moveToFirst();
        if(cursor.isAfterLast()){
            FastLog.d(databaseName+"表"+tableName+"中不存在要删除的数据");
            cursor.close();
            db.close();
            return false;
        }
        int count = cursor.getCount();
        try{
            db.beginTransaction();
            String deleteCommand = "delete from '"+tableName+"' "+filters;
            if(args!=null)
                for(String replaceStr: args)
                    deleteCommand = deleteCommand.replaceFirst("[?]","'"+replaceStr+"'");
            db.execSQL(deleteCommand);
            db.setTransactionSuccessful();
            FastLog.d(databaseName+"--d-"+count+"->"+tableName);
        }catch(SQLiteException e){
            FastLog.d("删除数据失败："+e.toString());
            return false;
        }finally{
            db.endTransaction();
            cursor.close();
            db.close();
        }
        return true;
    }

    /**
     * 更新单条数据
     * @param obj 更新对象
     * @return 是否成功更新
     */
    public boolean update(@NonNull Object obj){
        SQLiteDatabase db = prepare(null);
        String databaseName = db.getPath().substring(db.getPath().lastIndexOf(File.separator)+1);
        String tableName = obj.getClass().getCanonicalName();
        Field[] fields = obj.getClass().getDeclaredFields();
        ContentValues cv = new ContentValues();
        List<String> args = new ArrayList<>();
        String filter;
        String[] ss;

        if(!tableExists(db,tableName)){
            FastLog.d("更新数据失败，"+databaseName+"表"+tableName+"不存在");
            db.close();
            return false;
        }
        if(!tableHasData(db,tableName)){
            FastLog.d("更新数据失败，"+databaseName+"表"+tableName+"中不含任何数据");
            db.close();
            return false;
        }
        //先检测数据是否存在
        filter = getFilters(getPrimaryKeyName(obj.getClass()),mAttribute.getFilterCommand(),args);
        Cursor cursor = db.rawQuery("select *from '"+tableName+"'"+filter,ss = args.toArray(new String[]{}));
        cursor.moveToFirst();
        if(cursor.isAfterLast()){
            FastLog.d("更新数据失败,没有找到要更新的数据");
            cursor.close();
            db.close();
            return false;
        }
        int count = cursor.getCount();
        if(count>1){
            FastLog.d("更新数据失败,不支持多条数据同时更新");
            cursor.close();
            db.close();
            return false;
        }
        try{
            db.beginTransaction();
            for(Field field: fields){
                field.setAccessible(true);
                Class<?> type = field.getType();
                Database fieldInject = field.getAnnotation(Database.class);
                DatabaseFileRef fileRef = field.getAnnotation(DatabaseFileRef.class);
                String columnName = (fieldInject!=null && !TextUtils.isEmpty(fieldInject.columnName()))? fieldInject.columnName():field.getName();

                if(fieldInject!=null && fieldInject.ignore())
                    continue;
                if(fieldInject!=null && fieldInject.keyPrimary() && fieldInject.autoincrement())
                    continue;
                if(type==boolean.class)
                    cv.put(columnName,field.getBoolean(obj));
                else if(type==int.class)
                    cv.put(columnName,field.getInt(obj));
                else if(type==long.class)
                    cv.put(columnName,field.getLong(obj));
                else if(type==float.class)
                    cv.put(columnName,field.getFloat(obj));
                else if(type==double.class)
                    cv.put(columnName,field.getDouble(obj));
                else if(type==char.class){
                    char c = field.getChar(obj);
                    if(c==0)
                        cv.putNull(columnName);
                    else
                        cv.put(columnName,String.valueOf(c));
                }else if(type==short.class)
                    cv.put(columnName,field.getShort(obj));
                else if(type==byte.class)
                    cv.put(columnName,field.getByte(obj));
                else if(type==byte[].class)
                    cv.put(columnName,(byte[])field.get(obj));
                else if(type==String.class){
                    String str = (String)field.get(obj);
                    if(fileRef==null)
                        if (str == null)
                            cv.putNull(columnName);
                        else
                            cv.put(columnName,str);
                    else{
                        @SuppressLint("Range")
                        String value = cursor.getString(cursor.getColumnIndex(columnName));
                        File file = new File(value);
                        SaveUtil.saveToFile(file, str != null ? str.getBytes() : new byte[0],false);
                        cv.put(columnName,file.getAbsolutePath());
                    }
                }else{
                    Object pre = field.get(obj);
                    String json =new Gson().toJson(pre);
                    if(fileRef==null)
                        if(pre==null)
                            cv.putNull(columnName);
                        else
                            cv.put(columnName,json);
                    else{
                        @SuppressLint("Range")
                        String value = cursor.getString(cursor.getColumnIndex(columnName));
                        File file = new File(value);
                        SaveUtil.saveToFile(file,json.getBytes(),false);
                        cv.put(columnName,file.getAbsolutePath());
                    }
                }
            }
            filter = filter.substring(6); //削掉前面的where
            db.update("'"+tableName+"'",cv,filter,ss);
            db.setTransactionSuccessful();
            FastLog.d(databaseName+"--u-"+count+"->"+tableName);
        }catch(SQLiteException|IllegalAccessException|IllegalArgumentException|IOException e){
            FastLog.d("更新数据失败："+e.toString());
            return false;
        }finally{
            db.endTransaction();
            cursor.close();
            db.close();
        }
        return true;
    }

    /**
     * 保存对象到数据库
     * @param array 对象组
     * @return 如果存储成功返回true，否则为false
     */
    public boolean save(Object[] array){
        if(array==null || array.length<=0){
            return false;//没什么对象可存应该返回false吗？
        }
        Object firstObj = null;//首个非null对象
        for(Object obj: array)
            if(obj!=null){
                firstObj = obj;
                break;
            }
        if(firstObj==null){
            return false;
        }
        SQLiteDatabase db = prepare(null);
        String databaseName = db.getPath().substring(db.getPath().lastIndexOf(File.separator)+1);
        String tableName = firstObj.getClass().getCanonicalName();
        Field[] fields = firstObj.getClass().getDeclaredFields();
        ContentValues cv = new ContentValues();
        Field autoIncreKeyField;//自动增长的主键
        int totalCount = array.length;
        int removeCount = 0;
        int remainCount = 0;
        if(!tableExists(db,tableName)){
            FastLog.d("保存数据失败，"+databaseName+"表"+tableName+"不存在");
            db.close();
            return false;
        }
        try{
            db.beginTransaction();
            for(Object obj: array){
                if(obj==null){
                    removeCount++;
                    continue;
                }
                autoIncreKeyField = null;
                cv.clear();
                for(Field field: fields){
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    Database fieldInject = field.getAnnotation(Database.class);
                    DatabaseFileRef fileRef = field.getAnnotation(DatabaseFileRef.class);
                    String columnName = (fieldInject!=null && !TextUtils.isEmpty(fieldInject.columnName()))? fieldInject.columnName():field.getName();

                    if(fieldInject!=null && fieldInject.ignore())
                        continue;
                    if(fieldInject!=null && fieldInject.keyPrimary() && fieldInject.autoincrement()){
                        if(type==int.class){
                            int keyValue = field.getInt(obj);
                            if(keyValue<=0){
                                autoIncreKeyField = field;
                                continue;
                            }
                        }else if(type==long.class){
                            long keyValue = field.getLong(obj);
                            if(keyValue<=0){
                                autoIncreKeyField = field;
                                continue;
                            }
                        }
                    }
                    if(type==boolean.class)
                        cv.put(columnName,field.getBoolean(obj));
                    else if(type==int.class)
                        cv.put(columnName,field.getInt(obj));
                    else if(type==long.class)
                        cv.put(columnName,field.getLong(obj));
                    else if(type==float.class)
                        cv.put(columnName,field.getFloat(obj));
                    else if(type==double.class)
                        cv.put(columnName,field.getDouble(obj));
                    else if(type==char.class){
                        char c = field.getChar(obj);
                        if(c==0)
                            cv.putNull(columnName);
                        else
                            cv.put(columnName,String.valueOf(c));
                    }else if(type==short.class)
                        cv.put(columnName,field.getShort(obj));
                    else if(type==byte.class)
                        cv.put(columnName,field.getByte(obj));
                    else if(type==byte[].class)
                        cv.put(columnName,(byte[])field.get(obj));
                    else if(type==String.class){
                        String str = (String) field.get(obj);
                        if(fileRef==null)
                            if (str == null)
                                cv.putNull(columnName);
                            else
                                cv.put(columnName,str);
                        else{
                            File file = new File(sConfig.getFileRefDir(),String.format(Locale.getDefault(),
                                    "%s_%s_%s",firstObj.getClass().getSimpleName(),field.getName(),UUID.randomUUID()));
                            file.createNewFile();
                            SaveUtil.saveToFile(file, str != null ? str.getBytes() : new byte[0],false);
                            cv.put(columnName,file.getAbsolutePath());
                        }
                    }else{
                        Object pre = field.get(obj);
                        String json = new Gson().toJson(pre);
                        if(fileRef==null){
                            if(pre==null)
                                cv.putNull(columnName);
                            else
                                cv.put(columnName,json);
                        }else {
                            File file = new File(sConfig.getFileRefDir(),String.format(Locale.getDefault(),
                                    "%s_%s_%s",firstObj.getClass().getSimpleName(),field.getName(),UUID.randomUUID()));
                            file.createNewFile();
                            SaveUtil.saveToFile(file,json.getBytes(),false);
                            cv.put(columnName,file.getAbsolutePath());
                        }
                    }
                }
                long rowId = db.insertWithOnConflict("'"+tableName+"'",null,cv,SQLiteDatabase.CONFLICT_NONE);
                //对自动增长的主键赋值
                if(rowId!=-1 && autoIncreKeyField!=null){
                    Class<?> fieldType = autoIncreKeyField.getType();
                    if(fieldType==int.class)
                        autoIncreKeyField.setInt(obj,(int)rowId);
                    else if(fieldType==long.class)
                        autoIncreKeyField.setLong(obj,rowId);
                }
            }
            db.setTransactionSuccessful();
            remainCount = totalCount - removeCount;
            FastLog.d(databaseName+"--save-"+remainCount+"-(total="+totalCount+" remove="+removeCount+")->"+tableName);
        }catch(SQLiteException|IllegalAccessException|IllegalArgumentException|IOException e){
            FastLog.e("保存数据失败:"+e.getMessage());
            return false;
        }finally{
            db.endTransaction();
            db.close();
        }
        return true;
    }

    /**
     * 根据主键或过滤条件更新已存在的数据（如果主键存在不使用过滤条件）或存储不存在的数据
     * @param objs
     * @return
     */
    private boolean saveOrUpdate(Object[] objs){
        Object firstObj = null;
        boolean isUpdate = false;
        boolean success = false;

        if(objs==null || objs.length<=0)
            return false;
        //取第一个非null的对象
        for(Object object: objs){
            if(object!=null){
                firstObj = object;
                break;
            }
        }
        //有可能一个数组全是null,这种情况直接跳出
        if(firstObj==null)
            return false;
        SQLiteDatabase db = prepare(null);
        String tableName = firstObj.getClass().getCanonicalName();
        Field[] fields = firstObj.getClass().getDeclaredFields();
        Field keyField = null;//主键

        if(tableExists(db,tableName)){
            for(Field field: fields){
                field.setAccessible(true);
                Database tableInject = field.getAnnotation(Database.class);
                if(tableInject!=null && tableInject.keyPrimary()){
                    keyField = field;
                    break;
                }
            }
            if(keyField!=null){
                try{
                    Object keyValue = keyField.get(firstObj);
                    Object oldData = setFilter(And.condition(Condition.equal(Reflect.objToStr(keyValue)))).getFirst(firstObj.getClass());
                    if(oldData!=null){
                        isUpdate = true;
                        success = setFilter(And.condition(Condition.equal(Reflect.objToStr(keyValue)))).update(firstObj);
                    }
                }catch(IllegalAccessException e){
                    FastLog.e("数据库saveOrUpdate时出现异常:"+e.toString());
                    success = false;
                }
            }else{ //如果主键不存在，查询是否有过滤条件，如果这个有并且这个条件能查询到数据，则修改首个数据的值
                if(mAttribute.getFilterCommand()!=null){
                    Object oldData = getFirst(firstObj.getClass());
                    if(oldData!=null){
                        isUpdate = true;
                        success = update(firstObj);
                    }
                }
            }
        }
        if(!isUpdate){
            db = prepare(generateCreateTableSql(firstObj.getClass()));
            success = save(objs);
        }
        db.close();
        return success;
    }

    /**
     * 保存单多个对象、修改单个对象.支持传入数组、列表、映射
     * @param obj
     * @return
     */
    public boolean saveOrUpdate(Object obj){
        if(obj==null)
            return false;
        Object[] objs;
        if(obj instanceof Collection){
            Collection collection = (Collection)obj;
            objs = collection.toArray();
        }else if(obj instanceof Map){
            Map<?,?> map = (Map<?,?>)obj;
            Iterator<?> iter = map.keySet().iterator();
            int index = 0;
            objs = new Object[map.size()];
            while(iter.hasNext())
                objs[index++] = map.get(iter.next());
        }else if(obj.getClass().isArray())
            objs = (Object[])obj;
        else{
            //也许obj是一个普通引用
            objs = new Object[1];
            objs[0] = obj;
        }
        return saveOrUpdate(objs);
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取主键名
     * @param cla
     * @return
     */
    private String getPrimaryKeyName(Class<?> cla){
        Field[] fields = cla.getDeclaredFields();
        String key = null;
        for(Field f: fields){
            Database fieldInject = f.getAnnotation(Database.class);
            if(fieldInject!=null && fieldInject.keyPrimary()){
                key = (!TextUtils.isEmpty(fieldInject.columnName()))?fieldInject.columnName():f.getName();
                break;
            }
        }
        return TextUtils.isEmpty(key) ? null : key;
    }

    /**
     * @param field 字段名
     * @return 转换后的字段类型
     */
    private String getFieldTypeByConverted(@Nullable Field field) {
        String fieldType = null;
        if (field != null) {
            fieldType = field.getType().getCanonicalName();
            if (fieldType != null) {
                fieldType = fieldType
                        .replace(".", "_")
                        .replace("[]", "_array")
                ;
            }
        }
        return fieldType;
    }

    /**
     * 生成创建表sql语句
     * @param cla 对象
     * @return 创建表语句
     */
    private String generateCreateTableSql(Class<?> cla){
        StringBuilder sb = new StringBuilder();
        DatabaseTable table = loadAttribute(cla);

        sb.append("create table if not exists '"+table.tableName+"' (");

        Iterator<String> iter = table.columnMap.keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            DatabaseTable.DatabaseColumn column = table.columnMap.get(key);

            if(column.isIgnore)
                continue;
            sb.append(column.columnName+" "+column.type);
            if(column.isPrimaryKey)
                sb.append(" primary key");
            if(column.autoincrement){
                if(!column.type.equals("integer"))
                    throw new RuntimeException("自动增长只能用于整型数据");
                sb.append(" autoincrement");
            }
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(")");
        //FastLog.d("建表SQL语句:"+sb.toString());
        return sb.toString();
    }

    private DatabaseTable loadAttribute(Class<?> cla){
        DatabaseTable dt = new DatabaseTable();
        Field[] fields = cla.getDeclaredFields();
        dt.tableName = cla.getCanonicalName();

        for(Field field: fields){
            Database fieldInject = field.getAnnotation(Database.class);
            DatabaseTable.DatabaseColumn column = new DatabaseTable.DatabaseColumn();
            String fieldType = getFieldTypeByConverted(field);
            column.columnName = field.getName();
            column.type = Reflect.toSQLType(fieldType);
            if(fieldInject!=null){
                if(!TextUtils.isEmpty(fieldInject.columnName()))
                    column.columnName = fieldInject.columnName();
                if(fieldInject.keyPrimary()){
                    dt.keyColumn = column;
                    dt.keyFieldName = field.getName();
                }
                column.isPrimaryKey = fieldInject.keyPrimary();
                column.autoincrement = fieldInject.autoincrement();
                column.isIgnore = fieldInject.ignore();
                if(column.isPrimaryKey && !Reflect.isInteger(fieldType) && !Reflect.isReal(fieldType) && !Reflect.isVarchar(fieldType))
                    throw new UnsupportedOperationException("不支持成为主键的字段类型："+fieldType);
            }
            dt.columnMap.put(field.getName(),column);
        }
        return dt;
    }

    private SQLiteDatabase prepare(final String sql) throws SQLiteException{
        SQLiteDatabase database;
        SQLiteOpenHelper helper = new SQLiteOpenHelper(mContext,getCurrDatabaseNameComplete(),null,sConfig.getVersion()){

            @Override
            public void onCreate(SQLiteDatabase db){
                String databaseName = db.getPath().substring(db.getPath().lastIndexOf(File.separator)+1);
                FastLog.d("创建数据库:"+databaseName);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db,int oldVersion,
                                  int newVersion){
                FastLog.d("发现数据库版本需要升级，开始升级");
                if(mCustomUpdate!=null){
                    FastLog.d("使用自定义升级方案");
                    mCustomUpdate.update(db,oldVersion,newVersion);
                }else{
                    FastLog.d("使用内置升级方案");
                    updateDatabase(db);
                }
                FastLog.d("数据库升级完毕");
            }
        };

        database = helper.getWritableDatabase();
        if(!TextUtils.isEmpty(sql)){
            try{
                database.execSQL(sql);
            }catch(SQLiteException e){
                FastLog.e(e.getMessage());
            }
        }
        return database;
    }

    /**
     * 遍历所有table反射对象来作对比调整
     * @param db 指定数据库
     */
    private void updateDatabase(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table'",null);
        if(cursor!=null){
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String tableName = cursor.getString(0);
                cursor.moveToNext();
                //无视特殊表
                if(tableName.equals("android_metadata"))
                    continue;
                if(tableName.equals("sqlite_sequence"))
                    continue;
                checkTableChanged(db,tableName);
            }
            cursor.close();
        }
    }

    /**
     * 检查表对类映射.如果增加新列可以直接操作，但是如果是更改主键或者修改列类型删除列就需要表重建
     * @param db 指定数据库
     * @param tableName 指定数据表
     */
    private void checkTableChanged(SQLiteDatabase db,String tableName){
        String databaseName = db.getPath().substring(db.getPath().lastIndexOf(File.separator)+1);
        Class<?> cla;
        Field[] fields;
        Map<String,Field> fieldMap = new HashMap<>();
        Map<String,String> newColumnMap = new HashMap<>();
        List<String> retainColumns = new ArrayList<>(); //调整表结构时需要保留数据的列
        DatabaseTable table;
        Iterator<String> iter;
        boolean needRebuildTable=false;

        try{
            //如果对象类不存在则删除这张表
            cla = Class.forName(tableName);
        }catch(ClassNotFoundException e){
            db.execSQL("drop table '"+tableName+"'");
            FastLog.d(databaseName+"删除表"+tableName);
            return;
        }
        fields = cla.getDeclaredFields();
        for(Field field: fields){
            field.setAccessible(true);
            String columnName = field.getName(); //列名以注解为优先，默认字段名
            Database inject = field.getAnnotation(Database.class);
            if(inject!=null){
                if(inject.ignore())
                    continue;
                if(!TextUtils.isEmpty(inject.columnName()))
                    columnName = inject.columnName();
            }
            fieldMap.put(columnName,field);
        }
        table = parse(db,tableName);
        iter = table.columnMap.keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            DatabaseTable.DatabaseColumn column = table.columnMap.get(key);
            Database inject;
            Field field = fieldMap.remove(key);
            retainColumns.add(column.columnName);

            //也许类中某字段被删除了,重建表
            if(field==null){
                needRebuildTable = true;
                retainColumns.remove(column.columnName);
                continue;
            }
            //判断注解是否被修改
            inject = field.getAnnotation(Database.class);
            if(!column.isPrimaryKey){
                if(inject!=null && inject.keyPrimary()){
                    retainColumns.remove(column.columnName);
                    needRebuildTable = true; //不能保证某字段在成为主键之前数据唯一
                }
            }else{
                if(inject==null || !inject.keyPrimary())
                    needRebuildTable = true;
            }
            if(!column.autoincrement){
                if(inject!=null && inject.autoincrement()){
                    retainColumns.remove(column.columnName);
                    needRebuildTable = true; //不能保证某字段在成为主键之前数据唯一
                }
            }else{
                if(inject==null || !inject.autoincrement())
                    needRebuildTable = true;
            }
            //判断类型是否被修改.integer改为任何类型都可以被兼容,real只被varchar兼容,varchar不兼容其他类型
            String fieldType = getFieldTypeByConverted(field);
            switch(column.type){
                case "integer":
                    if(!Reflect.isInteger(fieldType))
                        needRebuildTable = true;
                    break;
                case "real":
                    if(!Reflect.isReal(fieldType)){
                        needRebuildTable = true;
                        if(!Reflect.isVarchar(fieldType))
                            retainColumns.remove(column.columnName);
                    }
                    break;
                case "varchar":
                    if(!Reflect.isVarchar(fieldType)){
                        needRebuildTable = true;
                        retainColumns.remove(column.columnName);
                    }
                    break;
                default:
                    if(!fieldType.equals(column.type)){
                        needRebuildTable = true;
                        retainColumns.remove(column.columnName);
                    }
                    break;
            }
        }
        //数据库表与类字段映射完后多余的字段将作为表新字段加入
        iter = fieldMap.keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            Field field = fieldMap.get(key);
            String fieldType = getFieldTypeByConverted(field);
            newColumnMap.put(key,Reflect.toSQLType(fieldType));
        }
        if(needRebuildTable || newColumnMap.size()>0){
            alterTable(db,cla,retainColumns,newColumnMap,needRebuildTable);
        } else {
            FastLog.d(databaseName+"表"+tableName+"不需要修改");
        }
    }

    private DatabaseTable parse(SQLiteDatabase db,String tableName){
        Cursor cursor = db.rawQuery("select name,sql from sqlite_master where name='"+tableName+"'",null);
        if(cursor!=null){
            cursor.moveToFirst();
            int nameIndex = cursor.getColumnIndex("name");
            int sqlIndex = cursor.getColumnIndex("sql");
            String name = cursor.getString(nameIndex);
            String sql = cursor.getString(sqlIndex);
            DatabaseTable dt = new DatabaseTable(name);
            sql = sql.substring(sql.indexOf('(')+1,sql.length()-1);
            String[] ss = sql.split(",");

            for(String s: ss){
                DatabaseTable.DatabaseColumn column = new DatabaseTable.DatabaseColumn();
                s = s.trim();
                column.columnName = s.substring(0,s.indexOf(' '));
                column.type = s.substring(s.indexOf(' ')).trim();
                if(column.type.indexOf(' ')!=-1)
                    column.type = column.type.substring(0,column.type.indexOf(' ')).trim();
                column.isPrimaryKey = s.contains("primary");
                column.autoincrement = s.contains("autoincrement");
                dt.columnMap.put(column.columnName,column);
            }
            cursor.close();
            return dt;
        }
        return null;
    }


    /**
     * 编译过滤语句
     * @param key 主键名
     * @param filterCommand 过滤命令
     * @param args 对应值
     * @return
     */
    private String getFilters(String key,FilterCommand filterCommand,List<String> args){
        StringBuilder command = new StringBuilder("");
        if(filterCommand==null)
            return command.toString();
        command.append(" where ").append(filterCommand.getFilterCondition().getExpression(key));
        if(filterCommand.getFilterCondition().getType()!=Condition.TYPE_NOT_NULL && filterCommand.getFilterCondition().getType()!=Condition.TYPE_NULL)
            args.add(filterCommand.getFilterCondition().getValue());
        filterCommand = filterCommand.getNext();
        while(filterCommand!=null){
            command.append(" ").append(filterCommand.getType()==FilterCommand.TYPE_AND ? "and" : "or").append(" ");
            command.append(filterCommand.getFilterCondition().getExpression(key));
            if(filterCommand.getFilterCondition().getType()!=Condition.TYPE_NOT_NULL && filterCommand.getFilterCondition().getType()!=Condition.TYPE_NULL)
                args.add(filterCommand.getFilterCondition().getValue());
            filterCommand = filterCommand.getNext();
        }
        return command.toString();
    }

    /**
     * 过滤要取的列
     * @return 要取的列
     */
    private String getSelectColumn(Class<?> cla){
        StringBuilder sb = new StringBuilder();
        String[] unSelect = mAttribute.getUnselectColumn();
        String[] select = mAttribute.getSelectColumn();
        if((unSelect==null || unSelect.length==0)){
            if((select==null || select.length==0))
                return "*";
            else{
                for(String s: select)
                    sb.append(s).append(",");
            }
        }else{
            List<String> columnNames = new ArrayList<>();
            Field[] fields = cla.getDeclaredFields();
            if(fields.length>0){
                for (Field field : fields) {
                    Database inject = field.getAnnotation(Database.class);
                    if (inject != null) {
                        if (inject.ignore()) {
                            continue;
                        }
                        String cm;
                        if (!TextUtils.isEmpty(inject.columnName())) {
                            cm = inject.columnName();
                        } else {
                            cm = field.getName();
                        }
                        columnNames.add(cm);
                    }
                }
            }
            if(!columnNames.isEmpty()){
                for(String filter: unSelect)
                    columnNames.remove(filter);
                for(String s:columnNames)
                    sb.append(s).append(",");
            }
        }
        if(sb.length()>0)
            sb.deleteCharAt(sb.length()-1);
        else
            sb.append("*");
        return sb.toString();
    }

    /**
     * 判断表是否存在
     * @param db 某数据库
     * @param tableName 表名
     * @return true某数据库存在指定表 false不存在
     */
    private boolean tableExists(SQLiteDatabase db,String tableName){
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' and name="+"'"+tableName+"'",null);
        boolean exists = !(cursor==null || cursor.getCount()<=0);
        //if(cursor!=null) cursor.close();
        //db.close();
        return exists;
    }

    /**
     * 判断表中是否有数据
     * @param db 某数据库
     * @param tableName 表名
     * @return true某数据库指定表有数据 false没有数据
     */
    private boolean tableHasData(SQLiteDatabase db,String tableName) {
        boolean hasData = false;
        if (tableExists(db,tableName)) {
            @SuppressLint("Recycle")
            Cursor cursor = db.rawQuery("select * from '" + tableName + "'", null);
            hasData = !(cursor == null || cursor.getCount() <= 0);
            //if (cursor != null) cursor.close();
            //db.close();
        }
        return hasData;
    }

    /**
     * 对一个存在的表进行修改
     * @param cla
     * @param valueToValue 保留列和数据
     * @param newColumn 新列名与类型映射组
     * @param needRebuildTable 是否需要重建表
     */
    public void alterTable(SQLiteDatabase db,Class<?> cla,List<String> valueToValue,Map<String,String> newColumn,boolean needRebuildTable){
        String databaseName = db.getPath().substring(db.getPath().lastIndexOf(File.separator)+1);
        String tableName = cla.getCanonicalName();
        String tempName = "temp_table_"+Long.toString(System.currentTimeMillis()); //数据转移用临时表
        Iterator<String> iter;

        if(!needRebuildTable){
            if(newColumn!=null && newColumn.size()>0){
                iter = newColumn.keySet().iterator();
                while(iter.hasNext()){
                    String column = iter.next();
                    String type = newColumn.get(column);
                    db.execSQL("alter table '"+tableName+"' add "+column+" "+type);
                }
                FastLog.d(databaseName+"表"+tableName+"增加"+Integer.toString(newColumn.size())+"列");
            }
        }else{
            if(valueToValue!=null && valueToValue.size()>0){
                StringBuilder sb = new StringBuilder();
                iter = valueToValue.iterator();
                while(iter.hasNext())
                    sb.append(iter.next()).append(",");
                if(valueToValue.size()>0)
                    sb.deleteCharAt(sb.length()-1);
                db.execSQL("alter table '"+tableName+"' rename to '"+tempName+"'");
                db.execSQL(generateCreateTableSql(cla));
                db.execSQL("insert into '"+tableName+"' ("+sb.toString()+") select "+sb.toString()+" from "+tempName);
                db.execSQL("drop table "+tempName);
            }else{
                db.execSQL("drop table '"+tableName+"'");
                db.execSQL(generateCreateTableSql(cla));
            }
            FastLog.d(databaseName+"表"+tableName+"被调整");
        }
    }

    /**
     * 删除当前数据库的某表
     * @param cla
     */
    public void dropTable(Class<?> cla){
        SQLiteDatabase db = mContext.openOrCreateDatabase(getCurrDatabaseNameComplete(),Context.MODE_PRIVATE,null);
        String tableName = cla.getCanonicalName();
        if(tableExists(db,tableName)){
            db.execSQL("drop table '"+tableName+"'");
            FastLog.d(db+"删除表"+tableName);
        }else{
            FastLog.d(db+"表"+tableName+"不存在");
        }
    }

    public String getCurrDatabaseNameComplete(){
        return TextUtils.isEmpty(mAttribute.getWhichDatabaseNameComplete()) ? sConfig.getDatabaseNameComplete() : mAttribute.getWhichDatabaseNameComplete();
    }

    /**
     * sql函数过滤
     * @param column 列名
     * @param functionCommand sql函数命令
     * @return
     */
    public FastDatabase putFunctionCommand(@NonNull String column,@NonNull FunctionCommand functionCommand){
        mFunctionCommand.put(column,functionCommand);
        return this;
    }

    /**
     * 自定义升级方法。不推荐使用，我们应该相信自动化，如果有需要实现高级功能的应修改自动化升级而不是使用自定义升级
     * @param custom
     */
    public void setCustomUpdate(CustomUpdate custom){
        mCustomUpdate = custom;
    }

    /**
     * 设置过滤条件
     * @param command
     * @return
     */
    public FastDatabase setFilter(FilterCommand command){
        mAttribute.setFilterCommand(command);
        return this;
    }

    /**
     * 增加过滤条件
     * @param command
     * @return
     */
    public FastDatabase addFilter(FilterCommand command){
        mAttribute.addFilterCommand(command);
        return this;
    }

    /**
     * 取数据时根据主键排序
     * @param asc
     * @return current database
     */
    public FastDatabase orderBy(boolean asc){
        mAttribute.setOrderAsc(asc);
        mAttribute.setOrderColumn(""); //空字符串代表使用主键字段
        return this;
    }

    /**
     * 排序
     * @param asc 如果是true为升序，反之降序
     * @param columnName
     * @return
     */
    public FastDatabase orderBy(boolean asc,String columnName){
        mAttribute.setOrderAsc(asc);
        mAttribute.setOrderColumn(columnName);
        return this;
    }

    /**
     * 取数据时行限制
     * @param start
     * @param size
     * @return current database
     */
    public FastDatabase limit(int start,int size){
        mAttribute.limit(start,size);
        return this;
    }

    /**
     * 仅取某些列字段
     * @param columns
     * @return current database
     */
    public FastDatabase select(String... columns){
        mAttribute.setSelectColumn(columns);
        return this;
    }

    /**
     * 不要某些列字段
     * @param columns
     * @return current database
     */
    public FastDatabase unselect(String... columns){
        mAttribute.setUnselectColumn(columns);
        return this;
    }

    /**
     * 非空构造给予参数。数据来自数据库使用DataFromDatabase
     * @param params
     * @return
     */
    public FastDatabase setConstructorParams(Object[] params){
        mAttribute.setConstructorParams(params);
        return this;
    }

    /**
     * 仅单次保存数据到指定数据库而不转换数据库
     * @param databaseName
     * @return
     */
    public FastDatabase toWhichDatabase(String databaseName){
        mAttribute.setToWhichDatabase(databaseName);
        return this;
    }

    /**
     * 指定操作的数据库,直到程序重新运行或者再调用此方法转换操作数据库对象
     * @param databaseName
     */
    public void switchDatabase(String databaseName){
        sConfig.switchDatabase(databaseName);
    }

    public static DatabaseConfig getConfig(){
        return DatabaseConfig.getInstance();
    }
}