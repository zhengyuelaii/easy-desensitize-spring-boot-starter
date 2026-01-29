package io.github.zhengyuelaii.desensitize.interceptor;

import io.github.zhengyuelaii.desensitize.advice.ResponseMaskingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-29
 */
public class DesensitizeInterceptorChainTest {

    private EasyDesensitizeInterceptor interceptor1;
    private EasyDesensitizeInterceptor interceptor2;

    private Object body;
    private ResponseMaskingContext context;
    private MethodParameter returnType;
    private ServerHttpRequest request;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        interceptor1 = mock(EasyDesensitizeInterceptor.class);
        interceptor2 = mock(EasyDesensitizeInterceptor.class);

        body = new Object();
        context = mock(ResponseMaskingContext.class);

        Method method = DummyController.class.getMethod("test");
        returnType = new MethodParameter(method, -1);

        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
    }

    @Test
    @DisplayName("所有 preHandle 返回 true 时，应继续执行")
    void should_continue_when_all_preHandle_true() {
        when(interceptor1.preHandle(any(), any(), any(), any(), any())).thenReturn(true);
        when(interceptor2.preHandle(any(), any(), any(), any(), any())).thenReturn(true);

        DesensitizeInterceptorChain chain =
                new DesensitizeInterceptorChain(Arrays.asList(interceptor1, interceptor2));

        boolean result = chain.preHandle(body, context, returnType, request, response);

        assertThat(result).isTrue();
        verify(interceptor1).preHandle(body, context, returnType, request, response);
        verify(interceptor2).preHandle(body, context, returnType, request, response);
    }

    @Test
    @DisplayName("某个 preHandle 返回 false 时，应中断执行")
    void should_stop_when_any_preHandle_false() {
        when(interceptor1.preHandle(any(), any(), any(), any(), any())).thenReturn(true);
        when(interceptor2.preHandle(any(), any(), any(), any(), any())).thenReturn(false);

        DesensitizeInterceptorChain chain =
                new DesensitizeInterceptorChain(Arrays.asList(interceptor1, interceptor2));

        boolean result = chain.preHandle(body, context, returnType, request, response);

        assertThat(result).isFalse();
        verify(interceptor1).preHandle(body, context, returnType, request, response);
        verify(interceptor2).preHandle(body, context, returnType, request, response);
    }

    @Test
    @DisplayName("postHandle 应按顺序调用所有拦截器")
    void should_call_all_postHandle() {
        DesensitizeInterceptorChain chain =
                new DesensitizeInterceptorChain(Arrays.asList(interceptor1, interceptor2));

        chain.postHandle(body, context, returnType, request, response);

        verify(interceptor1).postHandle(body, context, returnType, request, response);
        verify(interceptor2).postHandle(body, context, returnType, request, response);
    }

    @Test
    @DisplayName("onException 应通知所有拦截器")
    void should_call_onException_when_exception_occurred() {
        DesensitizeInterceptorChain chain =
                new DesensitizeInterceptorChain(Arrays.asList(interceptor1, interceptor2));

        Exception ex = new RuntimeException("test");

        chain.onException(ex, body, returnType, request, response);

        verify(interceptor1).onException(ex, body, returnType, request, response);
        verify(interceptor2).onException(ex, body, returnType, request, response);
    }

    static class DummyController {
        public Object test() {
            return null;
        }
    }

}
