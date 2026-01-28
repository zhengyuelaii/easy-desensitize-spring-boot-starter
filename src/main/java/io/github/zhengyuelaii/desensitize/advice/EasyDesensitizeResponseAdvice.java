package io.github.zhengyuelaii.desensitize.advice;

import io.github.zhengyuelaii.desensitize.annotation.IgnoreResponseMasking;
import io.github.zhengyuelaii.desensitize.annotation.ResponseMasking;
import io.github.zhengyuelaii.desensitize.interceptor.EasyDesensitizeInterceptor;
import io.github.zhengyuelaii.desensitize.config.EasyDesensitizeProperties;
import io.github.zhengyuelaii.desensitize.resolver.GlobalMaskingResolverComposite;
import io.github.zhengyuelaii.desensitize.core.EasyDesensitize;
import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
import io.github.zhengyuelaii.desensitize.core.handler.MaskingHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.*;

/**
 * 响应结果脱敏
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-15
 */
@ControllerAdvice
public class EasyDesensitizeResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(EasyDesensitizeResponseAdvice.class);

    @Autowired
    private EasyDesensitizeInterceptor interceptor;
    @Autowired
    private GlobalMaskingResolverComposite globalMaskingDataResolver;
    @Autowired
    private EasyDesensitizeProperties properties;

    /**
     * 判断是否支持拦截 这里检查方法或类上是否带有 @ResponseMasking 注解
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 1. 忽略脱敏（最高优先级）
        if (returnType.hasMethodAnnotation(IgnoreResponseMasking.class)
                || returnType.getContainingClass().isAnnotationPresent(IgnoreResponseMasking.class)) {
            return false;
        }

        // 2. 显式开启脱敏
        return returnType.hasMethodAnnotation(ResponseMasking.class)
                || returnType.getContainingClass().isAnnotationPresent(ResponseMasking.class);
    }

    /**
     * 在响应体写入之前执行数据脱敏处理
     *
     * @param body                  响应体对象
     * @param returnType            返回值类型参数
     * @param selectedContentType   选中的媒体类型
     * @param selectedConverterType 选中的HTTP消息转换器类型
     * @param request               服务器HTTP请求
     * @param response              服务器HTTP响应
     * @return 脱敏后的响应体对象，如果原响应体为空则返回null
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return null;
        }

        try {
            boolean shouldMask = interceptor.preHandle(body, returnType, request, response);
            if (shouldMask) {
                ResponseMaskingWrapper wrapper = new ResponseMaskingWrapper(returnType);
                Object data = body;
                if (properties.isUseGlobalResolver() && wrapper.isUseGlobalResolver()) {
                    // 全局数据解析
                    data = globalMaskingDataResolver.resolve(data);
                }
                // 执行脱敏
                EasyDesensitize.mask(data, null, wrapper.getHandlers(),
                        wrapper.getExcludedFields(), properties.isUseGlobalCache());
            }
            interceptor.postHandle(body, returnType, request, response);
        } catch (Exception e) {
            interceptor.onException(e, body, returnType, request, response);
            logger.error("An exception occurred during desensitization processing", e);
        }
        return body;
    }

    static class ResponseMaskingWrapper {

        private final boolean useGlobalResolver;
        private final Map<String, MaskingHandler> handlers;
        private final Set<String> excludedFields;

        public ResponseMaskingWrapper(MethodParameter returnType) {
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
}
