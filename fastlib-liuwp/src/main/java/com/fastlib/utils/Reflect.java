package com.fastlib.utils;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 使反射变得更简单容易
 * @author Sgfb
 */
public class Reflect{
	
	/**
	 * 使用方法:reflectString(对象,"对象.子对象.孙对象...")
	 * @param obj
	 * @param fieldName
	 * @return 反射回字符串
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static String reflectString(Object obj,String fieldName) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException{
		int index=-1;
		String content=null;
		Field field=null;
		
		if((index=fieldName.indexOf('.'))!=-1){
			String firstName=fieldName.substring(0, index);
			String lastName=fieldName.substring(index+1, fieldName.length());
			Field firstField=obj.getClass().getDeclaredField(firstName);
			Field lastField=null;
			Object obj2=null;
			
			firstField.setAccessible(true);
			obj2=firstField.get(obj);
			if(lastName.indexOf('.')!=-1){
				content=reflectString(obj2,lastName);
				return content;
			}
			lastField=obj2.getClass().getDeclaredField(lastName);
			lastField.setAccessible(true);
			content=(String)lastField.get(obj2);
		}
		else{
			field=obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			content=(String)field.get(obj);
		}
		return content;
	}

	/**
	 * 判断是否某族类
	 * @param field 要判断的字段
	 * @param patriarch 族长
     * @return
     */
	public static boolean isFamily(Field field,Class<?> patriarch){
		boolean result;
		try{
			result=field.getType().asSubclass(patriarch) != null;
		}catch(ClassCastException e){
			return false;
		}
		return result;
	}

	public static String objToStr(Object obj){
		if(obj instanceof Integer)
			return Integer.toString((int)obj);
		else if(obj instanceof String)
			return (String)obj;
		else if(obj instanceof Long)
			return Long.toString((long)obj);
		else if(obj instanceof Float)
			return Float.toString((float)obj);
		else if(obj instanceof Double)
			return Double.toString((double)obj);
		else if(obj instanceof Short)
			return Short.toString((short)obj);
		else
		    return obj.toString();
	}

	public static boolean isInteger(String type){
		if(type.equals("short")||type.equals("int")||type.equals("long"))
			return true;
		return false;
	}

	public static boolean isInteger(Field field){
		return isInteger(field.getType().getSimpleName());
	}

	public static boolean isReal(String type){
		if(type.equals("float")||type.equals("double"))
			return true;
		return false;
	}

	public static boolean isReal(Field field){
		return isReal(field.getType().getSimpleName());
	}

	public static boolean isVarchar(String type){
		if(type.equals("char")||type.equals("String"))
			return true;
		return false;
	}

	public static boolean isVarchar(Field field){
		return isVarchar(field.getType().getSimpleName());
	}

	public static String toSQLType(String type){
		if(isInteger(type))
			return "integer";
		if(isReal(type))
			return "real";
		if(isVarchar(type))
			return "varchar";
		return type;
	}

	/**
	 * 获取包括超类中的字段
	 * @param cla
	 * @return
	 */
	public static Field[] getAllField(Class<?> cla){
		List<Field> fields=new ArrayList<>();
		Object obj=new Object();
		Class<?> temp=cla;
		Field[] selfFiedls=cla.getDeclaredFields();

		for(int j=0;j<selfFiedls.length;j++)
			fields.add(selfFiedls[j]);
		while(!temp.getSuperclass().isInstance(obj)){
			Field[] fs=temp.getSuperclass().getDeclaredFields();
			temp=temp.getSuperclass();
			for(int i=0;i<fs.length;i++)
				fields.add(fs[i]);
		}
		return fields.toArray(new Field[0]);
	}

	/**
	 * 将某对象参数移到另一个对象中(不包括超类中的参数)
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean objToObj(Object from,Object to){
		if(from.getClass()!=to.getClass())
			return false;
		Field[] fields=to.getClass().getDeclaredFields();
		for(Field f:fields)
			try {
				f.set(to,f.get(from));
			} catch (IllegalAccessException e){
				Log.w("Reflect","objToObj exception:"+e.getMessage());
				return false;
			}
		return true;
	}

	/**
	 * 对比是否同一类型，基本对象与基本类型是同一类型
	 * @param obj1
	 * @param obj2
	 * @return
     */
	public static boolean equalBasicOrBasicObj(Class<?> obj1, Class<?> obj2){
		if(obj1==byte.class||obj1==Byte.class)
			return obj2==byte.class||obj2==Byte.class;
		if(obj1==char.class||obj1==Character.class)
			return obj2==char.class||obj2==Character.class;
		if(obj1==short.class||obj1==Short.class)
			return obj2==short.class||obj2==Short.class;
		if(obj1==int.class||obj1==Integer.class)
			return obj2==int.class||obj2==Integer.class;
		if(obj1==long.class||obj1==Long.class)
			return obj2==long.class||obj2==Long.class;
		if(obj1==float.class||obj1==Float.class)
			return obj2==float.class||obj2==Float.class;
		if(obj1==double.class||obj1==Double.class)
			return obj2==double.class||obj2==Double.class;
		return obj1==obj2;
	}
}