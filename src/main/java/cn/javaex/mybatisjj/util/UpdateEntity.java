package cn.javaex.mybatisjj.util;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装更新实体类
 * 
 * @author 陈霓清
 * @Date 2026年2月14日
 */
public class UpdateEntity {
	
	/**
	 * 创建代理实体类
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> clazz) {
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(clazz);
		factory.setInterfaces(new Class[] { Updatable.class });
		Class<?> proxyClass = factory.createClass();
		
		// 构造handler
		MethodHandler handler = new MethodHandler() {
			private final Map<String, Object> dirtyFields = new HashMap<>();
			
			@Override
			public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
				String name = thisMethod.getName();
				if (name.startsWith("set") && args != null && args.length == 1) {
					String field = name.substring(3, 4).toLowerCase() + name.substring(4);
					dirtyFields.put(field, args[0]);
				}
				if (name.equals("getModifiedFields") && (args == null || args.length == 0)) {
					return dirtyFields;
				}
				return proceed != null ? proceed.invoke(self, args) : null;
			}
		};
		
		try {
			Object instance = proxyClass.getDeclaredConstructor().newInstance();
			((javassist.util.proxy.ProxyObject) instance).setHandler(handler);
			return (T) instance;
		} catch (Exception e) {
			throw new RuntimeException("create proxy fail", e);
		}
	}
	
	// 静态内部接口
	public interface Updatable {
		Map<String, Object> getModifiedFields();
	}
}
