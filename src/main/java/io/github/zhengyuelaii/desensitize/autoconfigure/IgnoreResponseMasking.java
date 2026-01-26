package io.github.zhengyuelaii.desensitize.autoconfigure;

import java.lang.annotation.*;

/**
 * 忽略响应结果脱敏
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-15
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface IgnoreResponseMasking {
}
