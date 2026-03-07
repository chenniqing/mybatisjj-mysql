package cn.javaex.mybatisjj.config.interceptor;

public class DefaultBeforeModifiedSqlInterceptor implements BeforeModifiedSqlInterceptor {
	
	@Override
	public String modifiedSQL(String originalSql) {
		// 默认实现，不修改SQL
		return originalSql;
	}
	
}
