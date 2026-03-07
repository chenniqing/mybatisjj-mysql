package cn.javaex.mybatisjj.config.interceptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;

import cn.javaex.mybatisjj.pagehelper.PageHelper;
import cn.javaex.mybatisjj.util.MethodCacheUtils;

/**
 * 修改SQL拦截器
 * 
 * @author 陈霓清
 */
@Intercepts({
	@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class ModifiedSqlInterceptor implements Interceptor {

	/** 禁止使用分页的方法 */
	private String[] NOT_ENABLE_PAGINGS = {"selectById", "selectListByIds"};
	private static final Pattern LIMIT_PATTERN = Pattern.compile("\\s+limit\\s+\\?", Pattern.CASE_INSENSITIVE);
	
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
		String modifiedSql = beforeModifiedSqlInterceptor.modifiedSQL(originalSql);
		
		// 如果方法上有@NotEnablePaging注解，则不进行分页处理
		// 获取接口类和方法名
		String mapperInterface = ms.getId().substring(0, ms.getId().lastIndexOf("."));
		String mapperMethod = ms.getId().substring(ms.getId().lastIndexOf(".") + 1);
		Boolean hasNotEnablePagingAnnotation = MethodCacheUtils.hasNotEnablePagingAnnotation(mapperInterface, mapperMethod);
		if (Arrays.stream(NOT_ENABLE_PAGINGS).anyMatch(mapperMethod::equals) || hasNotEnablePagingAnnotation) {
			if (!originalSql.equals(modifiedSql)) {
				metaObject.setValue("delegate.boundSql.sql", modifiedSql);
			}
			return invocation.proceed();
		}
		
		PageHelper.Page page = PageHelper.getPage();
		if (Objects.nonNull(page) && !isCountQuery(modifiedSql) && page.getTotal()==null) {
			// 执行计数查询
			String countSql = "SELECT COUNT(1) FROM (" + modifiedSql + ") AS temp";
			Connection connection = (Connection) invocation.getArgs()[0];
			PreparedStatement countStmt = null;
			ResultSet rs = null;
			try {
				countStmt = connection.prepareStatement(countSql);
				// 清除原始 SQL 参数
				ParameterHandler parameterHandler = new DefaultParameterHandler(ms, boundSql.getParameterObject(), boundSql);
				parameterHandler.setParameters(countStmt);
				rs = countStmt.executeQuery();
				if (rs.next()) {
					int totalCount = rs.getInt(1);
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
			if (!LIMIT_PATTERN.matcher(modifiedSql).find()) {
				modifiedSql = modifiedSql + " LIMIT ?, ?";
				
				// 更新SQL语句
				metaObject.setValue("delegate.boundSql.sql", modifiedSql);
				
				// 获取现有参数，并添加分页参数
				List<ParameterMapping> parameterMappings = new ArrayList<>(boundSql.getParameterMappings());
				parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), "offset", Integer.class).build());
				parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), "limit", Integer.class).build());
				metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
				
				// 添加分页参数到BoundSql中的参数表中
				@SuppressWarnings("unchecked")
				Map<String, Object> additionalParameters = (Map<String, Object>) metaObject.getValue("delegate.boundSql.additionalParameters");
				additionalParameters.put("offset", (page.getPageNum() - 1) * page.getPageSize());
				additionalParameters.put("limit", page.getPageSize());
			} else {
				// 更新SQL语句
				if (!originalSql.equals(modifiedSql)) {
					metaObject.setValue("delegate.boundSql.sql", modifiedSql);
				}
			}
		}
		// 更新SQL语句
		else if (!originalSql.equals(modifiedSql)) {
			metaObject.setValue("delegate.boundSql.sql", modifiedSql);
		}
		
		return invocation.proceed();
	}

	// 判断是否是Count查询
	private boolean isCountQuery(String sql) {
		return sql.trim().toLowerCase().startsWith("select count(");
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
