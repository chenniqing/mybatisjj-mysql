package cn.javaex.mybatisjj.basic.common;

/**
 * 主键生成策略常量
 * 
 * @author 陈霓清
 */
public class IdTypeConstant {
	
	/** 主键自增 */
	public static final String AUTO = "auto";
	
	/** 32 位 UUID 字符串，不带- */
	public static final String UUID = "uuid";
	
	/** 长数字ID */
	public static final String LONG_ID = "long_id";
	
	/** 长数字ID 字符串类型 */
	public static final String LONG_ID_STR = "long_id_str";
	
}
