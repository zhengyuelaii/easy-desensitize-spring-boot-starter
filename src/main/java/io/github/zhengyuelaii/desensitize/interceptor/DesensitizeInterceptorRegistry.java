package io.github.zhengyuelaii.desensitize.interceptor;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器注册类
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-28
 */
public class DesensitizeInterceptorRegistry {

    private final List<DesensitizeInterceptorRegistration> registrations = new ArrayList<>();

    public DesensitizeInterceptorRegistration addInterceptor(EasyDesensitizeInterceptor interceptor) {
        DesensitizeInterceptorRegistration registration = new DesensitizeInterceptorRegistration(interceptor);
        this.registrations.add(registration);
        return registration;
    }

    public List<DesensitizeInterceptorRegistration> getRegistrations() {
        return this.registrations;
    }

}
