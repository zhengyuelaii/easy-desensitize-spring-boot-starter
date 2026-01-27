package io.github.zhengyuelaii.desensitize.autoconfigure;

import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNull;

/**
 * 测试 EasyDesensitizeResponseAdvice
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-27
 */
@ExtendWith(MockitoExtension.class)
public class EasyDesensitizeResponseAdviceTest {

    @InjectMocks
    private EasyDesensitizeResponseAdvice advice;

    @Mock
    private EasyDesensitizeInterceptor interceptor;

    @Mock
    private GlobalMaskingResolverComposite resolverComposite;

    @Mock
    private EasyDesensitizeProperties properties;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Test
    @DisplayName("应能处理数据为null")
    void should_handle_null_response_body() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("getUser");
        MethodParameter returnType = new MethodParameter(method, -1);
        Object result = advice.beforeBodyWrite(null, returnType, MediaType.APPLICATION_JSON,
                null, request, response);

        assertNull("当body为null时，应该返回null", result);
        verify(interceptor, never()).preHandle(any(), any(), any(), any());
        verify(interceptor, never()).postHandle(any(), any(), any(), any());
    }


    @Test
    @DisplayName("应能为数据脱敏")
    void should_mask_response_body() throws NoSuchMethodException {
        // given
        Map<String, Object> map = new HashMap<>();
        map.put("name", "李小龙");

        Method method = TestController.class.getMethod("getUser");
        MethodParameter returnType = new MethodParameter(method, -1);

        // mock
        when(properties.isUseGlobalResolver()).thenReturn(false);
        when(properties.isUseGlobalCache()).thenReturn(true);
        when(interceptor.preHandle(any(), any(), any(), any())).thenReturn(true);

        // when
        Object result = advice.beforeBodyWrite(map, returnType, MediaType.APPLICATION_JSON,
                null, request, response);

        // then
        assertThat(result).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) result).get("name")).isEqualTo("李*龙");
        verify(interceptor).preHandle(any(), eq(returnType), any(), any());
        verify(interceptor).postHandle(any(), eq(returnType), any(), any());
    }

    @Test
    @DisplayName("测试不需要脱敏的情况")
    void test_no_need_desensitize() throws NoSuchMethodException {
        Object originalBody = new Object();
        Method method = TestController.class.getMethod("getUser");
        MethodParameter returnType = new MethodParameter(method, -1);
        // mock
        when(interceptor.preHandle(any(), any(), any(), any())).thenReturn(false);
        // when
        Object result = advice.beforeBodyWrite(originalBody, returnType, MediaType.APPLICATION_JSON,
                null, request, response);

        assertThat(result).isSameAs(originalBody);
        verify(interceptor).preHandle(any(), eq(returnType), any(), any());
        verify(interceptor).postHandle(any(), eq(returnType), any(), any());
    }

    static class TestController {

        @ResponseMasking(fields = {
                @MaskingField(name = "name", typeHandler = KeepFirstAndLastHandler.class)
        })
        public Map<String, Object> getUser() {
            return null;
        }

    }

}
