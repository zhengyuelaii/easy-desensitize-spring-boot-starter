package com.github.zhengyuelaii.desensitize.autoconfigure;

import com.github.zhengyuelaii.desensitize.core.util.MaskingDataResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应数据脱敏注解
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-15
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ResponseMasking {

    Class<? extends MaskingDataResolver> resolver() default MaskingDataResolver.class;

}
