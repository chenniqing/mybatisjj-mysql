package cn.javaex.mybatisjj.basic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 租户ID字段
 * 
 * @author 陈霓清
 * @Date 2026年2月8日
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantId {
	
	/**
	 * 默认值，为空时取属性名称
	 */
	String value() default "";
	
}
