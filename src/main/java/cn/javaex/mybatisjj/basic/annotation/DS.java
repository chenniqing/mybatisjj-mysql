package cn.javaex.mybatisjj.basic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定当前Mapper、Mapper方法或实体类使用的数据源
 * 
 * @author 陈霓清
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DS {
	
	/**
	 * 数据源名称，对应DynamicDataSource中注册的数据源key
	 */
	String value();
	
}
