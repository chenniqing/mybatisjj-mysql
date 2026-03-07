package cn.javaex.mybatisjj.config.interceptor;

import org.springframework.stereotype.Component;

@Component
public class DefaultBeforeSaveEntityInterceptor implements BeforeSaveEntityInterceptor {

	@Override
	public void insertFill(Object entity) {
		
	}

	@Override
	public void updateFill(Object entity) {
		
	}
	
}
