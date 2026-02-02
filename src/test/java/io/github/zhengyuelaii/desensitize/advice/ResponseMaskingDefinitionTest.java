package io.github.zhengyuelaii.desensitize.advice;

import io.github.zhengyuelaii.desensitize.annotation.ResponseMasking;
import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.FixedMaskHandler;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * ResponseMaskingDefinition 测试
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-29
 */
public class ResponseMaskingDefinitionTest {
    /* ---------- 测试用 Controller ---------- */

    @ResponseMasking(
            useGlobalResolver = false,
            fields = {
                    @MaskingField(name = "name", typeHandler = KeepFirstAndLastHandler.class)
            },
            excludeFields = {"address"}
    )
    static class ClassLevelController {

        @ResponseMasking(
                useGlobalResolver = true,
                fields = {
                        @MaskingField(name = "password", typeHandler = FixedMaskHandler.class)
                },
                excludeFields = {"id"}
        )
        public String methodOverrideAll() {
            return "ok";
        }

        @ResponseMasking(
                useGlobalResolver = true,
                fields = {}
        )
        public String methodEmptyFields() {
            return "ok";
        }
    }

    /* ---------- 工具方法 ---------- */

    private MethodParameter getMethodParameter(String methodName) throws NoSuchMethodException {
        Method method = ClassLevelController.class.getMethod(methodName);
        return new MethodParameter(method, -1);
    }

    /* ---------- 测试用例 ---------- */

    @Test
    void should_method_override_class_useGlobalResolver() throws Exception {
        ResponseMaskingDefinition definition =
                new ResponseMaskingDefinition(getMethodParameter("methodOverrideAll"));

        assertThat(definition.isUseGlobalResolver()).isTrue();
    }

    @Test
    void should_use_method_fields_when_not_empty() throws Exception {
        ResponseMaskingDefinition definition =
                new ResponseMaskingDefinition(getMethodParameter("methodOverrideAll"));

        Map<String, MaskingHandler> handlers = definition.getHandlers();

        assertThat(handlers)
                .containsKey("password")
                .doesNotContainKey("name");
    }

    @Test
    void should_use_class_fields_when_method_fields_empty() throws Exception {
        ResponseMaskingDefinition definition =
                new ResponseMaskingDefinition(getMethodParameter("methodEmptyFields"));

        Map<String, MaskingHandler> handlers = definition.getHandlers();

        assertThat(handlers)
                .containsKey("name")
                .doesNotContainKey("password");
    }

    @Test
    void should_method_override_excludeFields() throws Exception {
        ResponseMaskingDefinition definition =
                new ResponseMaskingDefinition(getMethodParameter("methodOverrideAll"));

        Set<String> excludedFields = definition.getExcludedFields();

        assertThat(excludedFields)
                .containsExactly("id")
                .doesNotContain("address");
    }
}
