package com.test;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodDeserializer {

	/**
	 * 
	 * @param pkgName  包名+类名
	 * @param methodName  方法名称
	 * @param value  方法里的参数 （   类型一定要对     例： new Object[]{ "1", 1}   ）
	 * @return
	 */
	public static Object getValue(String pkgName, String methodName, Object[] value) {
		try {
    		Class<?> clazz = Class.forName(pkgName);
			Class<?>[] parameterTypes = new Class[value.length];  
		    for (int i = 0, j = value.length; i < j; i++) {  
		    	parameterTypes[i] = value[i].getClass();  
		    }  
			Method method=clazz.getDeclaredMethod(methodName, parameterTypes);   
			method.setAccessible(true)  ; 
			return method.invoke(clazz.newInstance(), value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  
		return null;
	}

    public static Object getValue(String pkgName, String methodName, Object value) {
    	try {
    		Class<?> clazz = Class.forName(pkgName);
			Method method=clazz.getDeclaredMethod(methodName, value.getClass());  
			method.setAccessible(true)  ;
			return method.invoke(clazz.newInstance(), value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  
    	return null;
    }

    public static Object getValue(String pkgName, String methodName) {
    	try {
    		Class<?> clazz = Class.forName(pkgName);
    		Method method=clazz.getDeclaredMethod(methodName);    
			method.setAccessible(true)  ;
    		return method.invoke(clazz.newInstance());
    	} catch (IllegalArgumentException e) {
    		e.printStackTrace();
    	} catch (NoSuchMethodException e) {
    		e.printStackTrace();
    	} catch (IllegalAccessException e) {
    		e.printStackTrace();
    	} catch (InvocationTargetException e) {
    		e.printStackTrace();
    	} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
    	return null;
    }

	public static void setValue(Object obj, String methodName, Object[] value) {
    	Class<?> clazz = obj.getClass();
    	try {
    		Class<?>[] parameterTypes = new Class[value.length];  
		    for (int i = 0, j = value.length; i < j; i++) {  
		    	parameterTypes[i] = value[i].getClass();  
		    }  
			Method method=clazz.getDeclaredMethod(methodName, parameterTypes);    
			method.setAccessible(true)  ;
			method.invoke(obj, value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    }
    
    public static void setValue(Object obj, String methodName, Object value) {
    	Class<?> clazz = obj.getClass();
    	try {
			Method method=clazz.getDeclaredMethod(methodName, value.getClass());    
			method.setAccessible(true)  ;
			method.invoke(obj, value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
    }

	public static void invoke(String pkgName, String methodName, Object[] value) {
		try {
    		Class<?> clazz = Class.forName(pkgName);
			Class<?>[] parameterTypes = new Class[value.length];  
			for (int i = 0, j = value.length; i < j; i++) {  
				parameterTypes[i] = value[i].getClass();  
			}  
			Method method=clazz.getDeclaredMethod(methodName, parameterTypes);   
			method.setAccessible(true)  ; 
			method.invoke(clazz.newInstance(), value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  
	}
	
    public static void invoke(String pkgName, String methodName, Object value) {
    	try {
    		Class<?> clazz = Class.forName(pkgName);
    		Method method=clazz.getDeclaredMethod(methodName, value.getClass());   
			method.setAccessible(true)  ; 
    		method.invoke(clazz.newInstance(), value);
    	} catch (IllegalArgumentException e) {
    		e.printStackTrace();
    	} catch (NoSuchMethodException e) {
    		e.printStackTrace();
    	} catch (IllegalAccessException e) {
    		e.printStackTrace();
    	} catch (InvocationTargetException e) {
    		e.printStackTrace();
    	} catch (InstantiationException e) {
    		e.printStackTrace();
    	} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  
    }
    
    public static void invoke(String pkgName, String methodName) {
    	try {
    		Class<?> clazz = Class.forName(pkgName);
    		Method method=clazz.getDeclaredMethod(methodName);    
			method.setAccessible(true)  ;
    		method.invoke(clazz.newInstance());
    	} catch (IllegalArgumentException e) {
    		e.printStackTrace();
    	} catch (NoSuchMethodException e) {
    		e.printStackTrace();
    	} catch (IllegalAccessException e) {
    		e.printStackTrace();
    	} catch (InvocationTargetException e) {
    		e.printStackTrace();
    	} catch (InstantiationException e) {
    		e.printStackTrace();
    	} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  
    }
}
