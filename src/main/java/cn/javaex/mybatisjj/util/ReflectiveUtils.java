package cn.javaex.mybatisjj.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射工具类
 * 
 * @author 陈霓清
 */
public class ReflectiveUtils {
	
	/**
	 * 获取类及其父类的所有字段
	 * @param clazz
	 * @return
	 */
	public static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			fields.addAll(0, Arrays.asList(c.getDeclaredFields()));
		}
		return fields;
	}

	/**
	 * 判断字段是否属于实体持久化字段
	 * <p>
	 * static 字段属于类级别元信息，例如 serialVersionUID，不应该映射成数据库列；
	 * transient 字段通常表示临时数据，也不应该参与增删改查 SQL。
	 * </p>
	 * @param field
	 * @return
	 */
	public static boolean isTableField(Field field) {
		int modifiers = field.getModifiers();
		return !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !field.isSynthetic();
	}

	/**
	 * 查找类及其父类中指定名称的字段
	 * @param clazz
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 */
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

	/**
	 * 获取字段值
	 * @param object
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(Object object, String fieldName) {
		Field field = getField(object.getClass(), fieldName);
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 设置字段值
	 * @param object
	 * @param fieldName
	 * @param value
	 */
	public static void setFieldValue(Object object, String fieldName, Object value) {
		Field field = getField(object.getClass(), fieldName);
		try {
			field.setAccessible(true);
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 递归获取字段
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
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
