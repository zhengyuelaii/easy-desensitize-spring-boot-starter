package com.github.zhengyuelaii.desensitize.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EasyDesensitizeAutoConfiguration {

    @Bean
    EasyDesensitizeResponseAdvice easyDesensitizeResponseAdvice() {
		return new EasyDesensitizeResponseAdvice();
	}
	
}
