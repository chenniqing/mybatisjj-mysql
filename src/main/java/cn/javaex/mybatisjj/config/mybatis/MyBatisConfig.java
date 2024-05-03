package cn.javaex.mybatisjj.config.mybatis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.javaex.mybatisjj.config.interceptor.BeforeSaveEntityInterceptor;
import cn.javaex.mybatisjj.config.interceptor.MybatisjjInterceptor;

@Configuration
public class MyBatisConfig {

	@Bean
	public MybatisjjInterceptor mybatisjjInterceptor(BeforeSaveEntityInterceptor beforeSaveEntityInterceptor) {
		return new MybatisjjInterceptor(beforeSaveEntityInterceptor);
	}

}
