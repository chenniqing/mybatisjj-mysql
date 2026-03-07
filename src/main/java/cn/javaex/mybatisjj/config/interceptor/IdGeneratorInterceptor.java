package cn.javaex.mybatisjj.config.interceptor;

import java.lang.reflect.Field;

/**
 * 主键策略接口
 * 
 * @author 陈霓清
 * @Date 2026年2月8日
 */
public interface IdGeneratorInterceptor {
	
	/**
	 * @param field     字段对象
	 * @param parameter 当前实体对象
	 * @return 生成的主键
	 */
	Object generate(Field field, Object parameter);
	
}
