package io.github.zhengyuelaii.desensitize.autoconfigure;

import io.github.zhengyuelaii.desensitize.advice.EasyDesensitizeResponseAdvice;
import io.github.zhengyuelaii.desensitize.config.EasyDesensitizeProperties;
import io.github.zhengyuelaii.desensitize.interceptor.DefaultDesensitizeInterceptor;
import io.github.zhengyuelaii.desensitize.interceptor.EasyDesensitizeInterceptor;
import io.github.zhengyuelaii.desensitize.resolver.AbstractMaskingDataResolver;
import io.github.zhengyuelaii.desensitize.resolver.GlobalMaskingResolverComposite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(EasyDesensitizeProperties.class)
@ConditionalOnProperty(prefix = "easy.desensitize", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    GlobalMaskingResolverComposite maskingResolverComposite(
            // Spring 会自动注入所有实现类，包括用户自定义的 @Component
            @Autowired(required = false) List<AbstractMaskingDataResolver<?>> resolvers) {
        GlobalMaskingResolverComposite composite = new GlobalMaskingResolverComposite();
        if (resolvers != null) {
            composite.addResolvers(resolvers);
        }
        return composite;
    }

}
