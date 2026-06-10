package cn.javaex.mybatisjj.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.javaex.mybatisjj.basic.annotation.DS;

/**
 * 数据源注解解析工具类
 * 
 * @author 陈霓清
 */
public class DataSourceAnnotationUtils {
	
	/**
	 * 空字符串占位，ConcurrentHashMap不允许缓存null
	 */
	private static final String EMPTY = "";
	
	/**
	 * 缓存MappedStatementId与数据源名称的对应关系
	 */
	private static final ConcurrentMap<String, String> DATA_SOURCE_CACHE = new ConcurrentHashMap<String, String>();
	
	/**
	 * 解析MappedStatement对应的数据源名称
	 * @param mappedStatementId
	 * @return
	 */
	public static String resolveDataSourceName(String mappedStatementId) {
		if (SqlStringUtils.isEmpty(mappedStatementId) || mappedStatementId.lastIndexOf(".") < 0) {
			return null;
		}
		
		String dataSourceName = DATA_SOURCE_CACHE.computeIfAbsent(mappedStatementId, DataSourceAnnotationUtils::resolveDataSourceNameInternal);
		return EMPTY.equals(dataSourceName) ? null : dataSourceName;
	}
	
	/**
	 * 解析普通 Spring 方法调用上的数据源名称，用于在事务拦截器之前切换数据源
	 * @param method 当前执行的方法
	 * @param targetClass 目标类
	 * @return 数据源名称
	 */
	public static String resolveDataSourceName(Method method, Class<?> targetClass) {
		if (method == null) {
			return null;
		}
		
		String methodDataSourceName = resolveAnnotationValue(method.getAnnotation(DS.class));
		if (SqlStringUtils.isNotEmpty(methodDataSourceName)) {
			return methodDataSourceName;
		}
		
		Method specificMethod = resolveSpecificMethod(method, targetClass);
		if (specificMethod != null && specificMethod.equals(method) == false) {
			methodDataSourceName = resolveAnnotationValue(specificMethod.getAnnotation(DS.class));
			if (SqlStringUtils.isNotEmpty(methodDataSourceName)) {
				return methodDataSourceName;
			}
		}
		
		String targetClassDataSourceName = resolveClassDataSourceName(targetClass);
		if (SqlStringUtils.isNotEmpty(targetClassDataSourceName)) {
			return targetClassDataSourceName;
		}
		
		return resolveClassDataSourceName(method.getDeclaringClass());
	}
	
	/**
	 * 实际解析数据源名称
	 * @param mappedStatementId
	 * @return
	 */
	private static String resolveDataSourceNameInternal(String mappedStatementId) {
		try {
			String mapperClassName = mappedStatementId.substring(0, mappedStatementId.lastIndexOf("."));
			String mapperMethodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(".") + 1);
			Class<?> mapperClass = Class.forName(mapperClassName);
			
			// 方法上的@DS优先级最高，适合一个Mapper中少数方法走不同数据源
			String methodDataSourceName = resolveMethodDataSourceName(mapperClass, mapperMethodName);
			if (SqlStringUtils.isNotEmpty(methodDataSourceName)) {
				return methodDataSourceName;
			}
			
			// Mapper类上的@DS优先级次之，适合整个Mapper绑定到一个数据源
			String mapperDataSourceName = resolveAnnotationValue(mapperClass.getAnnotation(DS.class));
			if (SqlStringUtils.isNotEmpty(mapperDataSourceName)) {
				return mapperDataSourceName;
			}
			
			// 实体类上的@DS作为兜底，适合按实体所属库划分Mapper
			Class<?> entityClass = resolveEntityClass(mapperClass);
			if (entityClass != null) {
				String entityDataSourceName = resolveAnnotationValue(entityClass.getAnnotation(DS.class));
				if (SqlStringUtils.isNotEmpty(entityDataSourceName)) {
					return entityDataSourceName;
				}
			}
		} catch (ClassNotFoundException e) {
			// 找不到 Mapper 类时按未配置数据源处理，避免框架内部打印堆栈污染业务日志
			return EMPTY;
		}
		return EMPTY;
	}
	
	/**
	 * 解析Mapper方法上的@DS
	 * @param mapperClass
	 * @param mapperMethodName
	 * @return
	 */
	private static String resolveMethodDataSourceName(Class<?> mapperClass, String mapperMethodName) {
		Method[] methods = mapperClass.getMethods();
		for (Method method : methods) {
			if (method.getName().equals(mapperMethodName)) {
				String dataSourceName = resolveAnnotationValue(method.getAnnotation(DS.class));
				if (SqlStringUtils.isNotEmpty(dataSourceName)) {
					return dataSourceName;
				}
			}
		}
		return null;
	}
	
	/**
	 * 解析注解中的数据源名称
	 * @param ds
	 * @return
	 */
	private static String resolveAnnotationValue(DS ds) {
		if (ds == null || SqlStringUtils.isEmpty(ds.value())) {
			return null;
		}
		return ds.value();
	}
	
	/**
	 * 获取代理背后的具体方法，优先读取实现类方法上的@DS
	 * @param method 接口或父类方法
	 * @param targetClass 目标类
	 * @return 具体方法
	 */
	private static Method resolveSpecificMethod(Method method, Class<?> targetClass) {
		if (targetClass == null) {
			return method;
		}
		try {
			return targetClass.getMethod(method.getName(), method.getParameterTypes());
		} catch (NoSuchMethodException e) {
			return method;
		}
	}
	
	/**
	 * 沿着类继承链和接口查找@DS，便于 service 类或 mapper 接口整体指定数据源
	 * @param clazz 类型
	 * @return 数据源名称
	 */
	private static String resolveClassDataSourceName(Class<?> clazz) {
		if (clazz == null || Object.class.equals(clazz)) {
			return null;
		}
		
		String dataSourceName = resolveAnnotationValue(clazz.getAnnotation(DS.class));
		if (SqlStringUtils.isNotEmpty(dataSourceName)) {
			return dataSourceName;
		}
		
		for (Class<?> interfaceClass : clazz.getInterfaces()) {
			dataSourceName = resolveClassDataSourceName(interfaceClass);
			if (SqlStringUtils.isNotEmpty(dataSourceName)) {
				return dataSourceName;
			}
		}
		
		return resolveClassDataSourceName(clazz.getSuperclass());
	}
	
	/**
	 * 从Mapper泛型中解析实体类
	 * @param mapperClass
	 * @return
	 */
	private static Class<?> resolveEntityClass(Class<?> mapperClass) {
		Type[] genericInterfaces = mapperClass.getGenericInterfaces();
		for (Type genericInterface : genericInterfaces) {
			Class<?> entityClass = resolveEntityClass(genericInterface);
			if (entityClass != null) {
				return entityClass;
			}
		}
		return null;
	}
	
	/**
	 * 从泛型类型中解析实体类
	 * @param type
	 * @return
	 */
	private static Class<?> resolveEntityClass(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class<?>) {
				return (Class<?>) actualTypeArguments[0];
			}
		}
		return null;
	}
	
}
