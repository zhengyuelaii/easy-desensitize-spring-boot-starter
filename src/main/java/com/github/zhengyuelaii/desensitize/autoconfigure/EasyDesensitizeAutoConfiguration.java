package com.github.zhengyuelaii.desensitize.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认配置
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-15
 */
@Configuration
public class EasyDesensitizeAutoConfiguration {

    @Bean
    EasyDesensitizeResponseAdvice easyDesensitizeResponseAdvice() {
		return new EasyDesensitizeResponseAdvice();
	}

    @Bean
    @ConditionalOnMissingBean
    EasyDesensitizeInterceptor easyDesensitizeInterceptor() {
		return new DefaultDesensitizeInterceptor();
	}
	
}
