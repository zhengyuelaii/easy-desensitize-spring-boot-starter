package com.github.zhengyuelaii.desensitize.autoconfigure;

import com.github.zhengyuelaii.desensitize.core.util.MaskingDataResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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

    @Bean
    MaskingResolverComposite maskingResolverComposite(
            // Spring 会自动注入所有实现类，包括用户自定义的 @Component
            @Autowired(required = false) List<AbstractMaskingDataResolver<?>> resolvers) {
        MaskingResolverComposite composite = new MaskingResolverComposite();
        if (resolvers != null) {
            composite.addResolvers(resolvers);
        }
        return composite;
    }

}
