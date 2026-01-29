package io.github.zhengyuelaii.desensitize.interceptor;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 脱敏拦截器注册包装
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-28
 */
public class DesensitizeInterceptorRegistration {

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    private final EasyDesensitizeInterceptor interceptor;

    private final List<String> includePatterns = new ArrayList<>();
    private final List<String> excludePatterns = new ArrayList<>();

    private int order = 0;

    public DesensitizeInterceptorRegistration(EasyDesensitizeInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public DesensitizeInterceptorRegistration addPathPatterns(String... patterns) {
        includePatterns.addAll(Arrays.asList(patterns));
        return this;
    }

    public DesensitizeInterceptorRegistration excludePatterns(String... patterns) {
        excludePatterns.addAll(Arrays.asList(patterns));
        return this;
    }

    public DesensitizeInterceptorRegistration order(int order) {
        this.order = order;
        return this;
    }

    public boolean match(String path) {
        return includePatterns.stream().anyMatch(p -> PATH_MATCHER.match(p, path))
                && excludePatterns.stream().noneMatch(p -> PATH_MATCHER.match(p, path));
    }

    public EasyDesensitizeInterceptor getInterceptor() {
        return interceptor;
    }

    public List<String> getIncludePatterns() {
        return includePatterns;
    }

    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "DesensitizeInterceptorRegistration{" +
                "interceptor=" + interceptor +
                ", includePatterns=" + includePatterns +
                ", excludePatterns=" + excludePatterns +
                ", order=" + order +
                '}';
    }
}
