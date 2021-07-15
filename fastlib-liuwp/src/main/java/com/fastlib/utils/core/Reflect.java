package com.fastlib.utils.core;

import androidx.annotation.Nullable;

import com.fastlib.utils.FastLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 使反射变得更简单容易
 * @author sgfb
 */
public class Reflect{

	public static Annotation findAnnotationByAnnotation(Annotation annotation,Class<? extends Annotation> target,List<Annotation> findHistory){
		if(annotation.annotationType()==target) return annotation;

		if(findHistory==null) findHistory=new ArrayList<>();
		if(findHistory.contains(annotation)) return null;

		Annotation[] as=annotation.annotationType().getAnnotations();
		if(as!=null){
			for(Annotation anno:as){
				findHistory.add(anno);
				Annotation targetAnno=findAnnotationByAnnotation(anno,target,findHistory);
				if(targetAnno!=null) return targetAnno;
			}
		}
		return null;
	}

	public static boolean isExtendsFrom(Class cla,Class specCla){
		Class parent=cla;
		do{
			parent=parent.getSuperclass();
			if(parent==specCla) return true;
		}
		while(parent!=Object.class&&parent!=null);
		return false;
	}

	public static @Nullable <T extends Annotation> Class checkParentClassHadAnnotation(Class cla,Class<T> annotation){
		Class parent=cla;
		do{
			if(parent.getAnnotation(annotation)!=null) return parent;
			parent=parent.getSuperclass();
		}while(parent!=null);
		return null;
	}

	/**
	 * 寻找当前类和向上寻找类注解
	 * @param cla 类
	 * @param anno 指定注解
	 * @param <T> 注解类型
	 * @return 如果不为空返回指定注解实体
	 */
	public static @Nullable
	<T extends Annotation> T upFindAnnotation(Class cla, Class<T> anno){
		return upFindAnnotation(cla,anno,false);
	}

	/**
	 * 寻找当前类和向上寻找类注解
	 * @param cla 类
	 * @param anno 指定注解
	 * @param <T> 注解类型
	 * @return 如果不为空返回指定注解实体
	 */
	public static @Nullable <T extends Annotation> T findAnnotation(Class cla, Class<T> anno){
		return findAnnotation(cla,anno,false);
	}

	/**
	 * 寻找当前类和向上寻找类注解
	 * @param cla 类
	 * @param anno 指定注解
	 * @param findInterface 是否查看接口注解
	 * @param <T> 注解类型
	 * @return 如果不为空返回指定注解实体
	 */
	public static @Nullable <T extends Annotation> T findAnnotation(Class cla,Class<T> anno,boolean findInterface){
		if(findInterface)
			return findCurrAnnotation(cla,anno);
		else{
			Annotation annoInstance;
			Class upwardClass=cla;

			do{
				annoInstance=upwardClass.getAnnotation(anno);
				if(annoInstance!=null) break;
				upwardClass=upwardClass.getSuperclass();
			}while(upwardClass!=null);
			return (T) annoInstance;
		}
	}

	/**
	 * 寻找当前类和向上寻找类注解
	 * @param cla 类
	 * @param anno 指定注解
	 * @param findInterface 是否查看接口注解
	 * @param <T> 注解类型
	 * @return 如果不为空返回指定注解实体
	 */
	public static @Nullable <T extends Annotation> T upFindAnnotation(Class cla, Class<T> anno, boolean findInterface){
		if(findInterface)
			return findCurrAnnotation(cla,anno);
		else{
			Annotation annoInstance;
			Class upwardClass=cla;

			do{
				annoInstance=upwardClass.getAnnotation(anno);
				if(annoInstance!=null) break;
				upwardClass=upwardClass.getSuperclass();
			}while(upwardClass!=null);
			return (T) annoInstance;
		}
	}

	private static <T extends Annotation> T findCurrAnnotation(Class cla,Class<T> anno){
		Annotation annoInstance=cla.getAnnotation(anno);

		if(annoInstance==null){
			if(cla.getSuperclass()!=null){
				annoInstance=findCurrAnnotation(cla.getSuperclass(),anno);
				if(annoInstance!=null) return (T) annoInstance;
			}
			if(cla.getInterfaces().length>0){
				for(Class inter:cla.getInterfaces()){
					annoInstance=findCurrAnnotation(inter,anno);
					if(annoInstance!=null) return (T) annoInstance;
				}
			}
		}
		return (T) annoInstance;
	}

	public static boolean instanceOfCheck(Class cla, Class superClass){
		Class workClass=cla.getSuperclass();

		while(workClass!=null&&workClass!=Object.class){
			if(workClass==superClass) return true;
		}
		return false;
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
		if(type.equals("char")||type.equals("java_lang_String")||type.equals("String"))
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
				FastLog.e("objToObj exception:"+e.getMessage());
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