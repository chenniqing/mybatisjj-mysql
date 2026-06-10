package cn.javaex.mybatisjj.config.mybatis;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import cn.javaex.mybatisjj.config.interceptor.BeforeModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.BeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.DataSourceAnnotationAdvisor;
import cn.javaex.mybatisjj.config.interceptor.DataSourceInterceptor;
import cn.javaex.mybatisjj.config.interceptor.DefaultBeforeModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.DefaultBeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.ModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.SaveEntityInterceptor;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class MyBatisInterceptorAutoConfiguration {

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static DataSourceInterceptor dataSourceInterceptor() {
		// 在SQL执行前切换当前线程的数据源
		return new DataSourceInterceptor();
	}
	
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static Advisor dataSourceAnnotationAdvisor() {
		// 在事务拦截器之前处理 @DS，避免事务提前绑定默认数据源连接
		return new DataSourceAnnotationAdvisor();
	}
	
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static ModifiedSqlInterceptor modifiedSqlInterceptor(
			@Autowired(required = false) BeforeModifiedSqlInterceptor beforeModifiedSqlInterceptor) {
		if (beforeModifiedSqlInterceptor == null) {
			beforeModifiedSqlInterceptor = new DefaultBeforeModifiedSqlInterceptor();
		}
		return new ModifiedSqlInterceptor(beforeModifiedSqlInterceptor);
	}
	
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static SaveEntityInterceptor saveEntityInterceptor(
			@Autowired(required = false) BeforeSaveEntityInterceptor beforeSaveEntityInterceptor) {
		if (beforeSaveEntityInterceptor == null) {
			beforeSaveEntityInterceptor = new DefaultBeforeSaveEntityInterceptor();
		}
		return new SaveEntityInterceptor(beforeSaveEntityInterceptor);
	}
	
}
