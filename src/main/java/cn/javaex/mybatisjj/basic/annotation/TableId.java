package cn.javaex.mybatisjj.basic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.javaex.mybatisjj.basic.common.IdTypeConstant;

/**
 * 标记主键字段
 * 
 * @author 陈霓清
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableId {
	
	/**
	 * 为空时将xxxYyy转为xxx_yyy的格式
	 */
	String value() default "";
	
	/**
	 * 主键生成策略
	 * "auto" 主键自增（默认）
	 * "uuid"  32 位 UUID 字符串，不带-
	 * "long_id" 长数字ID
	 * "long_id_str" 长数字ID 字符串类型
	 */
	String type() default IdTypeConstant.AUTO;
	
}
