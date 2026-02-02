package io.github.zhengyuelaii.desensitize.interceptor;

import io.github.zhengyuelaii.desensitize.advice.ResponseMaskingContext;
import io.github.zhengyuelaii.desensitize.advice.ResponseMaskingDefinition;
import org.springframework.core.MethodParameter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.util.List;

/**
 * 脱敏拦截器链
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-28
 */
public class DesensitizeInterceptorChain {

    private final List<EasyDesensitizeInterceptor> interceptors;

    public DesensitizeInterceptorChain(List<EasyDesensitizeInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public boolean preHandle(Object body, ResponseMaskingContext context, MethodParameter returnType, ServerHttpRequest request,
                             ServerHttpResponse response) {
        // 未定义任何拦截器，默认脱敏
        if (interceptors == null || interceptors.isEmpty()) {
            return true;
        }
        for (EasyDesensitizeInterceptor interceptor : interceptors) {
            if (!interceptor.preHandle(body, context, returnType, request, response)) {
                return false;
            }
        }
        return true;
    }

    public void postHandle(Object body, ResponseMaskingContext context, MethodParameter returnType, ServerHttpRequest request,
                           ServerHttpResponse response) {
        if (interceptors == null || interceptors.isEmpty())
            return;
        for (EasyDesensitizeInterceptor interceptor : interceptors) {
            interceptor.postHandle(body, context, returnType, request, response);
        }
    }

    public void onException(
            Exception ex,
            Object body,
            MethodParameter returnType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (interceptors == null || interceptors.isEmpty())
            return;
        for (EasyDesensitizeInterceptor interceptor : interceptors) {
            interceptor.onException(ex, body, returnType, request, response);
        }
    }
}
