package cn.javaex.mybatisjj.config.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.javaex.mybatisjj.config.interceptor.BeforeModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.BeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.DefaultBeforeModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.DefaultBeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.ModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.SaveEntityInterceptor;

@Configuration
public class MyBatisInterceptorAutoConfiguration {

	@Bean
	public ModifiedSqlInterceptor modifiedSqlInterceptor(
			@Autowired(required = false) BeforeModifiedSqlInterceptor beforeModifiedSqlInterceptor) {
		if (beforeModifiedSqlInterceptor == null) {
			beforeModifiedSqlInterceptor = new DefaultBeforeModifiedSqlInterceptor();
		}
		return new ModifiedSqlInterceptor(beforeModifiedSqlInterceptor);
	}
	
	@Bean
	public SaveEntityInterceptor saveEntityInterceptor(
			@Autowired(required = false) BeforeSaveEntityInterceptor beforeSaveEntityInterceptor) {
		if (beforeSaveEntityInterceptor == null) {
			beforeSaveEntityInterceptor = new DefaultBeforeSaveEntityInterceptor();
		}
		return new SaveEntityInterceptor(beforeSaveEntityInterceptor);
	}
	
}
