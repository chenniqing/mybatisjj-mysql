package cn.javaex.mybatisjj.config.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.javaex.mybatisjj.config.interceptor.BeforeModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.BeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.ModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.SaveEntityInterceptor;

@Configuration
public class MyBatisInterceptorAutoConfiguration {
	
	@Autowired
	private BeforeModifiedSqlInterceptor beforeModifiedSqlInterceptor;
	@Autowired
	private BeforeSaveEntityInterceptor beforeSaveEntityInterceptor;

	@Bean
	public ModifiedSqlInterceptor modifiedSqlInterceptor() {
		return new ModifiedSqlInterceptor(beforeModifiedSqlInterceptor);
	}
	
	@Bean
	public SaveEntityInterceptor saveEntityInterceptor() {
		return new SaveEntityInterceptor(beforeSaveEntityInterceptor);
	}
	
}
