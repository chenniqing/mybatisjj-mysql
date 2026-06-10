package cn.javaex.mybatisjj.config.interceptor;

import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import cn.javaex.mybatisjj.config.customize.DataSourceContextHolder;
import cn.javaex.mybatisjj.util.DataSourceAnnotationUtils;
import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * 多数据源切换拦截器
 * 
 * @author 陈霓清
 */
@Intercepts({
	@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
	@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }),
	@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class })
})
public class DataSourceInterceptor implements Interceptor {
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		
		// 根据Mapper方法、Mapper类或实体类上的@DS注解解析数据源名称
		String dataSourceName = DataSourceAnnotationUtils.resolveDataSourceName(ms.getId());
		if (SqlStringUtils.isEmpty(dataSourceName)) {
			return invocation.proceed();
		}
		
		// SQL执行前切换数据源，执行完成后恢复上一层数据源
		DataSourceContextHolder.push(dataSourceName);
		try {
			return invocation.proceed();
		} finally {
			DataSourceContextHolder.poll();
		}
	}
	
	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}
	
	@Override
	public void setProperties(Properties properties) {
		// 如有需要，这里可以配置属性
	}
	
}
