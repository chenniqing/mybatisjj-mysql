package cn.javaex.mybatisjj.config.interceptor;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.Ordered;

import cn.javaex.mybatisjj.config.customize.DataSourceContextHolder;
import cn.javaex.mybatisjj.util.DataSourceAnnotationUtils;
import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * @DS 注解切面顾问
 * 
 * @author 陈霓清
 */
public class DataSourceAnnotationAdvisor extends StaticMethodMatcherPointcutAdvisor {

	private static final long serialVersionUID = 1L;
	
	public DataSourceAnnotationAdvisor() {
		super(new DataSourceAnnotationMethodInterceptor());
		// 数据源必须在事务拦截器之前切换，否则事务管理器可能已经提前拿到了默认数据源连接
		setOrder(Ordered.HIGHEST_PRECEDENCE);
	}
	
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return SqlStringUtils.isNotEmpty(DataSourceAnnotationUtils.resolveDataSourceName(method, targetClass));
	}
	
	/**
	 * 在普通 Spring 方法调用进入前压入数据源上下文，支持 service 层 @DS 与 @Transactional 配合使用
	 */
	private static class DataSourceAnnotationMethodInterceptor implements MethodInterceptor {
		
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			Class<?> targetClass = invocation.getThis() == null ? null : invocation.getThis().getClass();
			String dataSourceName = DataSourceAnnotationUtils.resolveDataSourceName(invocation.getMethod(), targetClass);
			if (SqlStringUtils.isEmpty(dataSourceName)) {
				return invocation.proceed();
			}
			
			DataSourceContextHolder.push(dataSourceName);
			try {
				return invocation.proceed();
			} finally {
				DataSourceContextHolder.poll();
			}
		}
		
	}

}
