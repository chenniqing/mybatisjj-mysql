package cn.javaex.mybatisjj.util;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.javaex.mybatisjj.basic.annotation.NotEnablePaging;

/**
 * 方法缓存工具类
 * 
 * @author 陈霓清
 * @Date 2024年6月28日
 */
public class MethodCacheUtils {
	
	private static final ConcurrentMap<String, Boolean> METHOD_CACHE = new ConcurrentHashMap<>();
	
	public static Boolean hasNotEnablePagingAnnotation(String className, String methodName) {
		String key = className + "." + methodName;
		return METHOD_CACHE.computeIfAbsent(key, k -> {
			try {
				Class<?> clazz = Class.forName(className);
				Method[] methods = clazz.getMethods();
				for (Method method : methods) {
					if (method.getName().equals(methodName)) {
						return method.isAnnotationPresent(NotEnablePaging.class);
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return false;
		});
	}
	
}
