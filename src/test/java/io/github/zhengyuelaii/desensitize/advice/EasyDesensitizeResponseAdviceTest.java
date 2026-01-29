package io.github.zhengyuelaii.desensitize.advice;

import io.github.zhengyuelaii.desensitize.annotation.ResponseMasking;
import io.github.zhengyuelaii.desensitize.config.EasyDesensitizeProperties;
import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;
import io.github.zhengyuelaii.desensitize.interceptor.DesensitizeInterceptorRegistration;
import io.github.zhengyuelaii.desensitize.interceptor.DesensitizeInterceptorRegistry;
import io.github.zhengyuelaii.desensitize.interceptor.EasyDesensitizeInterceptor;
import io.github.zhengyuelaii.desensitize.resolver.GlobalMaskingResolverComposite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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
    private DesensitizeInterceptorRegistry registry;

    @Mock
    private DesensitizeInterceptorRegistration registration;

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
    @DisplayName("body 为 null 时应直接返回 null，且不触发任何处理")
    void should_handle_null_response_body() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("getUser");
        MethodParameter returnType = new MethodParameter(method, -1);

        Object result = advice.beforeBodyWrite(
                null,
                returnType,
                MediaType.APPLICATION_JSON,
                null,
                request,
                response
        );

        assertNull("当 body 为 null 时，应该返回 null", result);
        verifyNoInteractions(registry, resolverComposite, properties);
    }

    @Test
    @DisplayName("应根据 @ResponseMasking 正确对响应体进行脱敏")
    void should_mask_response_body() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", "李小龙");

        Method method = TestController.class.getMethod("getUser");
        MethodParameter returnType = new MethodParameter(method, -1);

        // mock 行为
        when(request.getURI()).thenReturn(new URI("http://localhost/test"));
        when(registry.getRegistrations()).thenReturn(new ArrayList<>());
        when(properties.isUseGlobalResolver()).thenReturn(false);
        when(properties.isUseGlobalCache()).thenReturn(true);

        // when
        Object result = advice.beforeBodyWrite(
                body,
                returnType,
                MediaType.APPLICATION_JSON,
                null,
                request,
                response
        );

        // then
        assertThat(result).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) result).get("name")).isEqualTo("李*龙");

        verify(properties).isUseGlobalResolver();
        verify(properties).isUseGlobalCache();

        verify(resolverComposite, never()).resolve(any());
    }

    @Test
    @DisplayName("当 interceptor 返回 false 时应跳过脱敏逻辑")
    void should_skip_desensitize_when_interceptor_blocked() throws Exception {
        // given
        Object originalBody = new Object();

        Method method = TestController.class.getMethod("getUser");
        MethodParameter returnType = new MethodParameter(method, -1);

        when(request.getURI()).thenReturn(new URI("http://localhost/test"));
        when(interceptor.preHandle(any(), any(), any(), any(), any())).thenReturn(false);
        when(registration.getInterceptor()).thenReturn(interceptor);
        when(registration.match(anyString())).thenReturn(true);
        when(registry.getRegistrations()).thenReturn(Collections.singletonList(registration));

        // when
        Object result = advice.beforeBodyWrite(
                originalBody,
                returnType,
                MediaType.APPLICATION_JSON,
                null,
                request,
                response
        );

        // then
        assertThat(result).isSameAs(originalBody);

        verify(interceptor).preHandle(any(), any(), eq(returnType), any(), any());
        verify(interceptor).postHandle(any(), any(), eq(returnType), any(), any());
        verifyNoInteractions(resolverComposite);
    }

    /**
     * 用于 MethodParameter 构造的测试 Controller
     */
    static class TestController {

        @ResponseMasking(fields = {
                @MaskingField(name = "name", typeHandler = KeepFirstAndLastHandler.class)
        })
        public Map<String, Object> getUser() {
            return null;
        }
    }
}
