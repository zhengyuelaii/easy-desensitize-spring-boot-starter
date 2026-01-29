package io.github.zhengyuelaii.desensitize.advice;

import io.github.zhengyuelaii.desensitize.annotation.ResponseMasking;
import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandlerFactory;
import org.springframework.core.MethodParameter;

import java.util.*;

/**
 * 响应数据脱敏包装器
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-28
 */
public class ResponseMaskingDefinition {

    private final boolean useGlobalResolver;
    private final Map<String, MaskingHandler> handlers;
    private final Set<String> excludedFields;

    protected ResponseMaskingDefinition(boolean useGlobalResolver, Map<String, MaskingHandler> handlers,
                                        Set<String> excludedFields) {
        this.useGlobalResolver = useGlobalResolver;
        this.handlers = handlers;
        this.excludedFields = excludedFields;
    }

    public ResponseMaskingDefinition(MethodParameter returnType) {
        ResponseMasking methodMasking =
                returnType.getMethodAnnotation(ResponseMasking.class);
        ResponseMasking classMasking =
                returnType.getContainingClass().getAnnotation(ResponseMasking.class);

        /* ---------- 1. useGlobalResolver：方法覆盖类 ---------- */
        if (methodMasking != null) {
            this.useGlobalResolver = methodMasking.useGlobalResolver();
        } else if (classMasking != null) {
            this.useGlobalResolver = classMasking.useGlobalResolver();
        } else {
            this.useGlobalResolver = true; // 全局默认值
        }

        /* ---------- 2. handlers（fields）：方法非空即覆盖 ---------- */
        MaskingField[] effectiveFields = null;

        if (methodMasking != null && methodMasking.fields().length > 0) {
            effectiveFields = methodMasking.fields();
        } else if (classMasking != null && classMasking.fields().length > 0) {
            effectiveFields = classMasking.fields();
        }

        if (effectiveFields != null) {
            this.handlers = new HashMap<>(effectiveFields.length);
            for (MaskingField field : effectiveFields) {
                this.handlers.put(
                        field.name(),
                        MaskingHandlerFactory.getHandler(field.typeHandler())
                );
            }
        } else {
            this.handlers = Collections.emptyMap();
        }

        /* ---------- 3. excludeFields：方法覆盖类 ---------- */
        if (methodMasking != null && methodMasking.excludeFields().length > 0) {
            this.excludedFields = new HashSet<>(
                    Arrays.asList(methodMasking.excludeFields())
            );
        } else if (classMasking != null && classMasking.excludeFields().length > 0) {
            this.excludedFields = new HashSet<>(
                    Arrays.asList(classMasking.excludeFields())
            );
        } else {
            this.excludedFields = Collections.emptySet();
        }
    }

    public boolean isUseGlobalResolver() {
        return useGlobalResolver;
    }

    public Map<String, MaskingHandler> getHandlers() {
        return handlers;
    }

    public Set<String> getExcludedFields() {
        return excludedFields;
    }
}
