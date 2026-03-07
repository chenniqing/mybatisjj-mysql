package cn.javaex.mybatisjj.config.mybatis;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.javaex.mybatisjj.config.interceptor.BeforeModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.BeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.DefaultBeforeModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.DefaultBeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.ModifiedSqlInterceptor;
import cn.javaex.mybatisjj.config.interceptor.SaveEntityInterceptor;

/**
 * 拦截器配置
 * 
 * @author 陈霓清
 * @Date 2024年7月14日
 */
@Configuration
public class MyBatisConfig {

	@Bean
	public Interceptor beforeModifiedSqlInterceptor(
			@Autowired(required = false) BeforeModifiedSqlInterceptor beforeModifiedSqlInterceptor) {
		if (beforeModifiedSqlInterceptor == null) {
			beforeModifiedSqlInterceptor = new DefaultBeforeModifiedSqlInterceptor();
		}
		return new ModifiedSqlInterceptor(beforeModifiedSqlInterceptor);
	}

	@Bean
	public Interceptor beforeSaveEntityInterceptor(
			@Autowired(required = false) BeforeSaveEntityInterceptor beforeSaveEntityInterceptor) {
		if (beforeSaveEntityInterceptor == null) {
			beforeSaveEntityInterceptor = new DefaultBeforeSaveEntityInterceptor();
		}
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
