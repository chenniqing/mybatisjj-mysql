package cn.javaex.mybatisjj.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射工具类
 * 
 * @author 陈霓清
 */
public class ReflectiveUtils {
	
	public static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			fields.addAll(0, Arrays.asList(c.getDeclaredFields()));
		}
		return fields;
	}

	public static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		Class<?> temp = clazz;
		while (temp != null && temp != Object.class) {
			for (Field field : temp.getDeclaredFields()) {
				if (field.getName().equals(fieldName)) {
					return field;
				}
			}
			temp = temp.getSuperclass();
		}
		throw new NoSuchFieldException(fieldName);
	}

	public static Object getFieldValue(Object object, String fieldName) {
		Field field = getField(object.getClass(), fieldName);
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setFieldValue(Object object, String fieldName, Object value) {
		Field field = getField(object.getClass(), fieldName);
		try {
			field.setAccessible(true);
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Field getField(Class<?> clazz, String fieldName) {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw new RuntimeException(e);
			} else {
				return getField(superClass, fieldName);
			}
		}
	}
}
