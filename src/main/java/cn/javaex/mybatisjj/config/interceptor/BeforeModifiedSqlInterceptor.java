package cn.javaex.mybatisjj.config.interceptor;

/**
 * 修改SQL接口
 * 
 * @author 陈霓清
 */
public interface BeforeModifiedSqlInterceptor {

	/**
	 * 修改SQL
	 * @param originalSql    原始SQL
	 * @return
	 */
	String modifiedSQL(String originalSql);

}
