package cn.javaex.mybatisjj.config.mybatis;

import java.util.Optional;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.javaex.mybatisjj.config.interceptor.BeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.BeforeModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.ModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.SaveEntityInterceptor;

@Configuration
public class MyBatisConfig {

	@Bean
	public Interceptor beforeModifiedSqlInterceptor(Optional<BeforeModifiedSqlInterceptor> beforeModifiedSqlInterceptor) {
		return new ModifiedSqlInterceptor(beforeModifiedSqlInterceptor);
	}
	
	@Bean
	public Interceptor beforeSaveEntityInterceptor(Optional<BeforeSaveEntityInterceptor> beforeSaveEntityInterceptor) {
		return new SaveEntityInterceptor(beforeSaveEntityInterceptor);
	}

	@Bean
	public org.apache.ibatis.session.Configuration mybatisConfiguration(Interceptor beforeModifiedSqlInterceptor, Interceptor beforeSaveEntityInterceptor) {
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		
		// 添加拦截器至拦截器链，顺序很重要
		configuration.addInterceptor(beforeModifiedSqlInterceptor);
		configuration.addInterceptor(beforeSaveEntityInterceptor);
		
		return configuration;
	}

}
