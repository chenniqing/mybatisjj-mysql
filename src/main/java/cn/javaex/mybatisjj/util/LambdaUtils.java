package cn.javaex.mybatisjj.util;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.javaex.mybatisjj.basic.annotation.ExcludeTableColumn;
import cn.javaex.mybatisjj.basic.annotation.TableColumn;
import cn.javaex.mybatisjj.model.function.SFunction;

/**
 * Lambda字段名提取工具
 * 
 * @author 陈霓清
 * @Date 2026年3月1日
 */
public class LambdaUtils {

	/**
	 * 用于缓存实体类的字段名
	 */
	private static final ConcurrentHashMap<Class<?>, Map<String, String>> COLUMN_CACHE = new ConcurrentHashMap<>();

	/**
	 * 解析字段名
	 * @param <T>
	 * @param <R>
	 * @param lambda
	 * @return
	 */
	public static <T, R> String resolveFieldName(SFunction<T, R> lambda) {
		try {
			SerializedLambda sl = serializedLambda(lambda);
			
			// 1.从 lambda 拿到实体类
			Class<?> entityClass = resolveImplClass(sl);
			
			// 2.从 getter/isxx 推导属性名
			String property = resolvePropertyName(sl.getImplMethodName());
			
			// 3.属性名 -> 列名（注解优先，否则默认驼峰转下划线）
			return resolveColumnName(entityClass, property);
		} catch (Exception e) {
			throw new RuntimeException("Lambda field name parsing failed", e);
		}
	}

	/**
	 * 序列化Lambda
	 * @param lambda
	 * @return
	 * @throws Exception
	 */
	private static SerializedLambda serializedLambda(Object lambda) throws Exception {
		Method m = lambda.getClass().getDeclaredMethod("writeReplace");
		m.setAccessible(true);
		return (SerializedLambda) m.invoke(lambda);
	}

	/**
	 * 解析实现类
	 * @param sl
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static Class<?> resolveImplClass(SerializedLambda sl) throws ClassNotFoundException {
		// implClass 形如: cn/javaex/.../SysMenuEntity
		String className = sl.getImplClass().replace('/', '.');
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return Class.forName(className, false, cl);
	}

	/**
	 * 解析属性名
	 * @param implMethodName
	 * @return
	 */
	private static String resolvePropertyName(String implMethodName) {
		String name = implMethodName;
		if (name.startsWith("get") && name.length() > 3) {
			name = name.substring(3);
		} else if (name.startsWith("is") && name.length() > 2) {
			name = name.substring(2);
		}
		// 用 Introspector 更标准：URL -> URL / uRL 等边界更符合 JavaBean
		return Introspector.decapitalize(name);
	}

	/**
	 * 属性名 -> 列名
	 * @param entityClass
	 * @param property
	 * @return
	 */
	private static String resolveColumnName(Class<?> entityClass, String property) {
		Map<String, String> map = COLUMN_CACHE.computeIfAbsent(entityClass, LambdaUtils::initColumnMap);
		String col = map.get(property);
		if (col == null) {
			throw new IllegalArgumentException("Field mapping not found: " + entityClass.getName() + "#" + property);
		}
		return col;
	}

	/**
	 * 列初始化
	 * @param entityClass
	 * @return
	 */
	private static Map<String, String> initColumnMap(Class<?> entityClass) {
		// - @ExcludeTableColumn 排除
		// - @TableColumn(value="xxx") 指定列名
		// - 否则默认驼峰转下划线
		Map<String, String> map = new java.util.HashMap<>();
		for (Field f : ReflectiveUtils.getAllFields(entityClass)) {
			if (!ReflectiveUtils.isTableField(f) || f.isAnnotationPresent(ExcludeTableColumn.class)) {
				continue;
			}
			String fieldName = f.getName();

			TableColumn tc = f.getAnnotation(TableColumn.class);
			if (tc != null && !SqlStringUtils.isEmpty(tc.value())) {
				map.put(fieldName, tc.value());
			} else {
				map.put(fieldName, SqlStringUtils.toUnderlineName(fieldName)); // 默认策略：转下划线
			}
		}
		
		return Collections.unmodifiableMap(map);
	}
}
