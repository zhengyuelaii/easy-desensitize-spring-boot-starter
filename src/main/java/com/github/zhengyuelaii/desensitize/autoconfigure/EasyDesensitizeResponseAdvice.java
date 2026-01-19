package com.github.zhengyuelaii.desensitize.autoconfigure;

import com.github.zhengyuelaii.desensitize.core.EasyDesensitize;
import com.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import com.github.zhengyuelaii.desensitize.core.handler.MaskingHandler;
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

import java.util.HashMap;
import java.util.Map;

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
    @Autowired(required = false)
    private AbstractMaskingDataResolver<?> globalMaskingDataResolver;

    /**
     * 判断是否支持拦截 这里检查方法或类上是否带有 @ResponseMasking 注解
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
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
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null) {
            return null;
        }

        if (globalMaskingDataResolver != null && globalMaskingDataResolver.supports(body)) {
            body = globalMaskingDataResolver.resolve(body);
        }

        if (interceptor != null) {
            try {
                boolean shouldMask = interceptor.preHandle(body, returnType, request, response);
                if (shouldMask) {
                    // 执行脱敏
                    EasyDesensitize.mask(body, getMaskingHandlerMap(returnType));
                }
                interceptor.postHandle(body, returnType, request, response);
            } catch (Exception e) {
                logger.error("An exception occurred during desensitization processing", e);
            }
        }
        return body;
    }

    private Map<String, MaskingHandler> getMaskingHandlerMap(MethodParameter returnType) {
        Map<String, MaskingHandler> maskingHandlerMap = null;

        ResponseMasking responseMasking = returnType.getMethodAnnotation(ResponseMasking.class);
        if (responseMasking == null) {
            responseMasking = returnType.getContainingClass().getAnnotation(ResponseMasking.class);
        }
        if (responseMasking != null) {
            MaskingField[] fields = responseMasking.fields();
            if (fields != null && fields.length > 0) {
                maskingHandlerMap = new HashMap<>();
                for (MaskingField field : fields) {
                    try {
                        maskingHandlerMap.put(field.name(), field.typeHandler().newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        logger.error("Failed to instantiate masking handler for field: " + field.name(), e);
                    }
                }
            }
        }
        return maskingHandlerMap;
    }

}
