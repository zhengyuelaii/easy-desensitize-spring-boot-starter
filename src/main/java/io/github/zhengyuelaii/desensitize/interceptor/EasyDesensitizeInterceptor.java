package io.github.zhengyuelaii.desensitize.interceptor;

import org.springframework.core.MethodParameter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

/**
 * 数据脱敏拦截器接口
 * 用于定义数据脱敏处理的拦截回调方法
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-15
 */
public interface EasyDesensitizeInterceptor {

    /**
     * 预处理请求的回调方法
     * 在请求处理之前执行，用于进行前置检查、验证或设置等操作
     *
     * @param body       请求体对象，包含客户端发送的数据
     * @param returnType 方法返回类型参数，用于获取返回值的相关信息
     * @param request    服务器HTTP请求对象，提供请求相关的方法和属性
     * @param response   服务器HTTP响应对象，用于构建和发送响应
     * @return boolean 返回true表示继续执行后续处理逻辑，返回false表示中断请求处理流程
     */
    default boolean preHandle(Object body, MethodParameter returnType, ServerHttpRequest request,
                              ServerHttpResponse response) {
        return true;
    }


    /**
     * 处理请求后的回调方法
     *
     * @param body       响应体对象，包含处理后的响应数据
     * @param returnType 方法返回值的类型参数信息
     * @param request    服务器HTTP请求对象
     * @param response   服务器HTTP响应对象
     */
    default void postHandle(Object body, MethodParameter returnType, ServerHttpRequest request,
                            ServerHttpResponse response) {
        // 默认不执行操作
    }

    /**
     * 异常回调（脱敏过程中发生异常）
     *
     * <p>
     * 注意：异常已被框架捕获，默认行为是记录日志并返回原始响应。
     * 实现方可在此进行监控、降级、打点等操作。
     * </p>
     */
    default void onException(
            Exception ex,
            Object body,
            MethodParameter returnType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
    }

}
