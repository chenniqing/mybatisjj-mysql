package cn.javaex.mybatisjj.config.customize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.javaex.mybatisjj.basic.common.IdTypeConstant;
import cn.javaex.mybatisjj.config.interceptor.IdGeneratorInterceptor;
import cn.javaex.mybatisjj.util.SqlIdUtils;

/**
 * 注册主键策略
 * 
 * @author 陈霓清
 * @Date 2026年2月8日
 */
public class IdGeneratorRegistry {
	private static final Map<String, IdGeneratorInterceptor> registry = new ConcurrentHashMap<>();

	// 内置几个常用的
	static {
		registry.put(IdTypeConstant.UUID, (field, parameter) -> SqlIdUtils.getUUID());
		registry.put(IdTypeConstant.LONG_ID, (field, parameter) -> SqlIdUtils.getLongId());
		registry.put(IdTypeConstant.LONG_ID_STR, (field, parameter) -> SqlIdUtils.getLongIdStr());
	}

	// 允许用户注册自己的策略
	public static void register(String type, IdGeneratorInterceptor generator) {
		registry.put(type, generator);
	}

	// 获取策略
	public static IdGeneratorInterceptor get(String type) {
		return registry.get(type);
	}
}