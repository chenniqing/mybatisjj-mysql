package cn.javaex.mybatisjj.config.interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * insert和update执行前的拦截器接口
 * 
 * @author 陈霓清
 */
public interface BeforeSaveEntityInterceptor {
	
	/**
	 * insert操作时要填充的字段
	 * @param entity
	 */
	void insertFill(Object entity);
	
	/**
	 * update操作时要填充的字段
	 * @param entity
	 */
	void updateFill(Object entity);
	
	/**
	 * 属性值填充（当值为NULL时，才填充）
	 * @param entity
	 * @param fieldName
	 * @param parameter
	 */
	default void fieldFillWhenNull(Object entity, String fieldName, Object parameter) {
		if (entity instanceof Map) {
			Object value = ((Map<?, ?>) entity).get("list");
			List<?> list = (List<?>) value;
			
			for (Object object : list) {
				this.setFieldValueWhenNull(object, fieldName, parameter);
			}
		} else if (parameter != null) {
			this.setFieldValueWhenNull(entity, fieldName, parameter);
		}
	}
	
	default void setFieldValueWhenNull(Object entity, String fieldName, Object parameter) {
		Class<?> clazz = entity.getClass();
		
		try {
			// 1. 判断该属性是否存在
			Field field = clazz.getDeclaredField(fieldName);
			
			// 2. 判断该属性是否已经有值了，有值的情况下，不再赋值
			Method method = clazz.getMethod("get" + SqlStringUtils.capitalize(fieldName));
			if ((method == null) || (method.invoke(entity) != null)) {
				return;
			}
			
			// 3. 赋值
			field.setAccessible(true);
			field.set(entity, parameter);
			field.setAccessible(false);
		} catch (NoSuchFieldException e) {
			// 字段不存在，直接跳过
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 属性值填充
	 * @param entity
	 * @param fieldName
	 * @param parameter
	 */
	default void fieldFill(Object entity, String fieldName, Object parameter) {
		if (entity instanceof Map) {
			if (((Map<?, ?>) entity).containsKey("list") == false) {
				// 说明执行的是 updateNullColumnById 方法，直接返回
				return;
			}
			
			Object value = ((Map<?, ?>) entity).get("list");
			List<?> list = (List<?>) value;
			
			for (Object object : list) {
				this.setFieldValue(object, fieldName, parameter);
			}
		} else if (parameter != null) {
			this.setFieldValue(entity, fieldName, parameter);
		}
	}
	
	default void setFieldValue(Object entity, String fieldName, Object parameter) {
		Class<?> clazz = entity.getClass();
		
		try {
			// 1. 判断该属性是否存在
			Field field = clazz.getDeclaredField(fieldName);
			
			// 2. 赋值
 			field.setAccessible(true);
 			field.set(entity, parameter);
 			field.setAccessible(false);
		} catch (NoSuchFieldException e) {
			// 字段不存在，直接跳过
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
