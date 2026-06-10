package cn.javaex.mybatisjj.config.interceptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;

import cn.javaex.mybatisjj.basic.common.DbType;
import cn.javaex.mybatisjj.config.dialect.DbDialect;
import cn.javaex.mybatisjj.config.dialect.DbDialectRegistry;
import cn.javaex.mybatisjj.pagehelper.PageHelper;
import cn.javaex.mybatisjj.util.DbTypeUtils;
import cn.javaex.mybatisjj.util.DynamicTableNameUtils;
import cn.javaex.mybatisjj.util.MethodCacheUtils;

/**
 * 修改SQL拦截器
 * 
 * @author 陈霓清
 */
@Intercepts({
		@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
public class ModifiedSqlInterceptor implements Interceptor {

	/** 禁止使用分页的方法 */
	private String[] NOT_ENABLE_PAGINGS = { "selectById", "selectListByIds" };

	private BeforeModifiedSqlInterceptor beforeModifiedSqlInterceptor;

	public ModifiedSqlInterceptor(BeforeModifiedSqlInterceptor beforeModifiedSqlInterceptor) {
		this.beforeModifiedSqlInterceptor = beforeModifiedSqlInterceptor;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		if ((invocation.getTarget() instanceof StatementHandler) == false) {
			return invocation.proceed();
		}
		
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
		
		// 获取MappedStatement
		MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
		
		BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
		// 原始SQL
		String originalSql = boundSql.getSql();
		// 修改后的SQL
		String modifiedSql = originalSql;
		if (beforeModifiedSqlInterceptor != null) {
			String tempSql = beforeModifiedSqlInterceptor.modifiedSQL(originalSql);
			if (tempSql != null && tempSql.trim().isEmpty() == false) {
				modifiedSql = tempSql;
			}
		}
		
		// 根据当前线程的动态表名上下文替换SQL中的逻辑表名
		modifiedSql = DynamicTableNameUtils.replaceTableName(modifiedSql);
		
		// 如果方法上有@NotEnablePaging注解，则不进行分页处理
		// 获取接口类和方法名
		String mapperInterface = ms.getId().substring(0, ms.getId().lastIndexOf("."));
		String mapperMethod = ms.getId().substring(ms.getId().lastIndexOf(".") + 1);
		Boolean hasNotEnablePagingAnnotation = MethodCacheUtils.hasNotEnablePagingAnnotation(mapperInterface, mapperMethod);
		PageHelper.Page page = PageHelper.getPage();
		if (Arrays.stream(NOT_ENABLE_PAGINGS).anyMatch(mapperMethod::equals)
				|| Boolean.TRUE.equals(hasNotEnablePagingAnnotation)) {
			/*
			 * 本次 SELECT 明确不参与分页时，需要区分两种情况：
			 * 1. startPage 后第一个查询就是 selectById/selectListByIds，这时清理分页上下文，避免后续查询误分页；
			 * 2. 分页查询已经被第一个 SELECT 消费，后续循环 selectById 不能清理上下文，否则 PageInfo 读取不到 total。
			 */
			if (ms.getSqlCommandType() == SqlCommandType.SELECT && page != null && page.isUsed() == false) {
				PageHelper.clearPage();
			}
			if (!originalSql.equals(modifiedSql)) {
				metaObject.setValue("delegate.boundSql.sql", modifiedSql);
			}
			return invocation.proceed();
		}
		
		if (Objects.nonNull(page) && page.isUsed() == false && ms.getSqlCommandType() == SqlCommandType.SELECT && !isCountQuery(modifiedSql) && page.getTotal() == null) {
			Connection connection = (Connection) invocation.getArgs()[0];
			DbType dbType = DbTypeUtils.getDbType(connection);
			DbDialect dbDialect = DbDialectRegistry.getDialect(dbType);
			// 执行计数查询
			String countSql = dbDialect.buildCountSql(modifiedSql);
			PreparedStatement countStmt = null;
			ResultSet rs = null;
			try {
				countStmt = connection.prepareStatement(countSql);
				// 设置原始 SQL 参数
				ParameterHandler parameterHandler = new DefaultParameterHandler(ms, boundSql.getParameterObject(), boundSql);
				parameterHandler.setParameters(countStmt);
				rs = countStmt.executeQuery();
				if (rs.next()) {
					long totalCount = rs.getLong(1);
					page.setTotal(Long.valueOf(totalCount));
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (countStmt != null) {
					countStmt.close();
				}
			}
			
			// 修改为分页 SQL
			long offset = (Long.valueOf(page.getPageNum()) - 1L) * Long.valueOf(page.getPageSize());
			long limit = Long.valueOf(page.getPageSize());
			modifiedSql = dbDialect.buildPaginationSql(modifiedSql, offset, limit);
			// 一个 startPage 只消费一次 SELECT，避免用户忘记 new PageInfo 时影响同线程后续查询
			page.setUsed(true);
			
			// 更新SQL语句
			metaObject.setValue("delegate.boundSql.sql", modifiedSql);
		}
		// 更新SQL语句
		else if (!originalSql.equals(modifiedSql)) {
			metaObject.setValue("delegate.boundSql.sql", modifiedSql);
		}
		
		try {
			return invocation.proceed();
		} catch (Throwable e) {
			// 查询异常时 PageInfo 不会被创建，这里兜底清理分页上下文
			if (page != null) {
				PageHelper.clearPage();
			}
			throw e;
		}
	}
	
	// 判断是否是Count查询
	private boolean isCountQuery(String sql) {
		if (sql == null) {
			return false;
		}
		
		String normalizedSql = sql.trim().toLowerCase();
		return normalizedSql.startsWith("select count(");
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
