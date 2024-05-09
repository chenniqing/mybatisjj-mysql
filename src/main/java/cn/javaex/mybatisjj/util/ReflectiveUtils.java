package cn.javaex.mybatisjj.util;

import java.lang.reflect.Field;

public class ReflectiveUtils {
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
