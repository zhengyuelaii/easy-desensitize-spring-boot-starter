package io.github.zhengyuelaii.desensitize.advice;

import io.github.zhengyuelaii.desensitize.annotation.IgnoreResponseMasking;
import io.github.zhengyuelaii.desensitize.annotation.ResponseMasking;
import io.github.zhengyuelaii.desensitize.config.EasyDesensitizeProperties;
import io.github.zhengyuelaii.desensitize.core.EasyDesensitize;
import io.github.zhengyuelaii.desensitize.interceptor.DesensitizeInterceptorChain;
import io.github.zhengyuelaii.desensitize.interceptor.DesensitizeInterceptorRegistration;
import io.github.zhengyuelaii.desensitize.interceptor.DesensitizeInterceptorRegistry;
import io.github.zhengyuelaii.desensitize.interceptor.EasyDesensitizeInterceptor;
import io.github.zhengyuelaii.desensitize.resolver.GlobalMaskingResolverComposite;
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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private DesensitizeInterceptorRegistry interceptorRegistry;
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

        // 拦截器处理
        String path = request.getURI().getPath();
        List<EasyDesensitizeInterceptor> matchInterceptor = interceptorRegistry.getRegistrations().stream()
                .filter(r -> r.match(path))
                .sorted(Comparator.comparingInt(DesensitizeInterceptorRegistration::getOrder))
                .map(DesensitizeInterceptorRegistration::getInterceptor)
                .collect(Collectors.toList());

        DesensitizeInterceptorChain chain = new DesensitizeInterceptorChain(matchInterceptor);

        try {
            ResponseMaskingContext context = new ResponseMaskingContext(new ResponseMaskingDefinition(returnType));
            boolean shouldMask = chain.preHandle(body, context, returnType, request, response);
            if (shouldMask) {
                Object data = body;
                if (properties.isUseGlobalResolver() && context.isUseGlobalResolver()) {
                    // 全局数据解析
                    data = globalMaskingDataResolver.resolve(data);
                }
                // 执行脱敏
                EasyDesensitize.mask(data, null, context.getEffectiveHandlers(),
                        context.getEffectiveExcludedFields(), properties.isUseGlobalCache());
            }
            chain.postHandle(body, context, returnType, request, response);
        } catch (Exception e) {
            chain.onException(e, body, returnType, request, response);
            logger.error("An exception occurred during desensitization processing", e);
        }
        return body;
    }
}
