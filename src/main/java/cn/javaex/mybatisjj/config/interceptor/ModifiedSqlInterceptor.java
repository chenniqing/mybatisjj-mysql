package cn.javaex.mybatisjj.config.interceptor;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * 修改SQL拦截器
 * 
 * @author 陈霓清
 */
@Intercepts({
	@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class ModifiedSqlInterceptor implements Interceptor {

	private BeforeModifiedSqlInterceptor beforeModifiedSqlInterceptor;
	
	public ModifiedSqlInterceptor(BeforeModifiedSqlInterceptor beforeModifiedSqlInterceptor) {
		this.beforeModifiedSqlInterceptor = beforeModifiedSqlInterceptor;
	}
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		if (beforeModifiedSqlInterceptor != null) {
			if (invocation.getTarget() instanceof StatementHandler) {
				StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
				MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
				BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
				// 原始SQL
				String originalSql = boundSql.getSql();
				// 修改后的SQL
				String modifiedSql = beforeModifiedSqlInterceptor.modifiedSQL(originalSql);
				
				// 更新SQL语句
				if (!originalSql.equals(modifiedSql)) {
					metaObject.setValue("delegate.boundSql.sql", modifiedSql);
				}
			}
		}

		// 继续执行后面的拦截器链和原来的查询
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		// 可用于传递配置的属性，如果有的话
	}

}
