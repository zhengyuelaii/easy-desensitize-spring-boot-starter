package com.github.zhengyuelaii.desensitize.autoconfigure;

import com.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
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

    /**
     * 脱敏解析器类，默认为 {@link MaskingDataResolver}
     *
     * @return 脱敏解析器类
     */
    Class<? extends MaskingDataResolver> resolver() default MaskingDataResolver.class;


    /**
     * 需要脱敏的字段配置，为空默认采用字段中配置的脱敏规则
     * 
     * @return 脱敏字段配置数组
     */
    MaskingField[] fields() default {};

}
