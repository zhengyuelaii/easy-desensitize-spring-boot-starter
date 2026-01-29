package io.github.zhengyuelaii.desensitize.advice;

import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 响应脱敏上下文
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-29
 */
public class ResponseMaskingContext {

    private final ResponseMaskingDefinition definition;

    private final Map<String, MaskingHandler> overrideHandlers;

    private final Set<String> removeHandlers;

    private final Set<String> excludedFields;

    private final Set<String> includedFields;

    public ResponseMaskingContext(ResponseMaskingDefinition definition) {
        this.definition = definition;
        this.overrideHandlers = new HashMap<>();
        this.removeHandlers = new HashSet<>();
        this.excludedFields = new HashSet<>();
        this.includedFields = new HashSet<>();
    }

    public Map<String, MaskingHandler> getBaseHandlers() {
        return definition.getHandlers();
    }

    public Set<String> getBaseExcludedFields() {
        return definition.getExcludedFields();
    }

    public ResponseMaskingContext addHandler(String field, MaskingHandler handler) {
        overrideHandlers.put(field, handler);
        removeHandlers.remove(field);
        return this;
    }

    public ResponseMaskingContext removeHandler(String field) {
        overrideHandlers.remove(field);
        removeHandlers.add(field);
        return this;
    }

    public ResponseMaskingContext addExcludedField(String field) {
        excludedFields.add(field);
        includedFields.remove(field);
        return this;
    }

    public ResponseMaskingContext removeExcludedField(String field) {
        excludedFields.remove(field);
        includedFields.add(field);
        return this;
    }

    public Map<String, MaskingHandler> getEffectiveHandlers() {
        Map<String, MaskingHandler> handlers = new HashMap<>(getBaseHandlers());
        // 移除被标记删除的 Handler
        removeHandlers.forEach(handlers::remove);
        // 合并自定义 Handler
        handlers.putAll(overrideHandlers);
        return handlers;
    }

    public Set<String> getEffectiveExcludedFields() {
        Set<String> merged = new HashSet<>(getBaseExcludedFields());
        // 移除被标记删除的字段
        merged.removeAll(includedFields);
        // 添加被标记保留的字段
        merged.addAll(excludedFields);
        return merged;
    }

    public boolean isUseGlobalResolver() {
        return definition.isUseGlobalResolver();
    }
}
