package com.github.zhengyuelaii.desensitize.autoconfigure;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.github.zhengyuelaii.desensitize.core.EasyDesensitize;

@ControllerAdvice
public class EasyDesensitizeResponseAdvice implements ResponseBodyAdvice<Object> {

	/**
	 * 判断是否支持拦截 这里检查方法或类上是否带有 @ResponseMasking 注解
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.hasMethodAnnotation(ResponseMasking.class)
				|| returnType.getContainingClass().isAnnotationPresent(ResponseMasking.class);
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		
		if (body == null) {
			return null;
		}
		
		// 执行脱敏
		EasyDesensitize.mask(body);
		
		return body;
	}

}
