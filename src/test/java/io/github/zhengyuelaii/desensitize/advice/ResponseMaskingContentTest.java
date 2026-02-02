package io.github.zhengyuelaii.desensitize.advice;

import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * ResponseMaskingContent 测试
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-29
 */
public class ResponseMaskingContentTest {

    static class DummyHandler implements MaskingHandler {
        @Override
        public String getMaskingValue(String value) {
            return "***";
        }
    }

    private ResponseMaskingDefinition mockDefinition() {
        Map<String, MaskingHandler> handlers = new HashMap<>();
        handlers.put("name", new DummyHandler());
        handlers.put("mobile", new DummyHandler());
        Set<String> excludedFields = new HashSet<>();
        excludedFields.add("idCard");
        return new ResponseMaskingDefinition(true, handlers, excludedFields);
    }

    @Test
    @DisplayName("应使用 Definition 中的基础规则")
    void should_use_base_definition() {
        ResponseMaskingContext context = new ResponseMaskingContext(mockDefinition());

        Map<String, MaskingHandler> handlers = context.getEffectiveHandlers();
        Set<String> excluded = context.getEffectiveExcludedFields();

        assertThat(handlers).containsKeys("name", "mobile");
        assertThat(excluded).containsExactly("idCard");
        assertThat(context.isUseGlobalResolver()).isTrue();
    }

    @Test
    @DisplayName("应允许覆盖已有 Handler")
    void should_override_handler() {
        ResponseMaskingContext context =
                new ResponseMaskingContext(mockDefinition());

        MaskingHandler newHandler = value -> "MASKED";

        context.addHandler("name", newHandler);

        Map<String, MaskingHandler> handlers = context.getEffectiveHandlers();

        assertThat(handlers.get("name")).isSameAs(newHandler);
        assertThat(handlers).containsKey("mobile");
    }

    @Test
    @DisplayName("应允许移除注解中定义的 Handler")
    void should_remove_base_handler() {
        ResponseMaskingContext context =
                new ResponseMaskingContext(mockDefinition());

        context.removeHandler("mobile");

        Map<String, MaskingHandler> handlers = context.getEffectiveHandlers();

        assertThat(handlers).doesNotContainKey("mobile");
        assertThat(handlers).containsKey("name");
    }

    @Test
    @DisplayName("应允许动态新增排除字段")
    void should_add_excluded_field() {
        ResponseMaskingContext context =
                new ResponseMaskingContext(mockDefinition());

        context.addExcludedField("email");

        Set<String> excluded = context.getEffectiveExcludedFields();

        assertThat(excluded).contains("idCard", "email");
    }

    @Test
    @DisplayName("应允许恢复被注解排除的字段")
    void should_restore_excluded_field() {
        ResponseMaskingContext context =
                new ResponseMaskingContext(mockDefinition());

        context.removeExcludedField("idCard");

        Set<String> excluded = context.getEffectiveExcludedFields();

        assertThat(excluded).doesNotContain("idCard");
    }

    @Test
    @DisplayName("多次增删改操作应正确合并")
    void should_merge_operations_correctly() {
        ResponseMaskingContext context =
                new ResponseMaskingContext(mockDefinition());

        MaskingHandler override = value -> "OVERRIDE";

        context
                .removeHandler("mobile")
                .addHandler("email", override)
                .addExcludedField("address")
                .removeExcludedField("idCard");

        Map<String, MaskingHandler> handlers = context.getEffectiveHandlers();
        Set<String> excluded = context.getEffectiveExcludedFields();

        assertThat(handlers)
                .containsKey("name")
                .containsKey("email")
                .doesNotContainKey("mobile");

        assertThat(handlers.get("email")).isSameAs(override);

        assertThat(excluded)
                .contains("address")
                .doesNotContain("idCard");
    }

}
