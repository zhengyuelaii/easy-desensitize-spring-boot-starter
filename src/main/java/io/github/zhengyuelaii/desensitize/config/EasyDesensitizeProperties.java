package io.github.zhengyuelaii.desensitize.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasyDesensitize 配置类
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-26
 */
@ConfigurationProperties("easy.desensitize")
public class EasyDesensitizeProperties {

    /**
     * 是否启用脱敏功能，默认为true
     */
    private boolean enabled = true;

    /**
     * 是否使用全局缓存，默认为true
     */
    private boolean useGlobalCache = true;

    /**
     * 是否使用全局解析器，默认为true
     */
    private boolean useGlobalResolver = true;

    /**
     * 失败策略，默认为FAIL_OPEN
     */
    private FailureStrategy failureStrategy = FailureStrategy.FAIL_OPEN;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isUseGlobalCache() {
        return useGlobalCache;
    }

    public void setUseGlobalCache(boolean useGlobalCache) {
        this.useGlobalCache = useGlobalCache;
    }

    public boolean isUseGlobalResolver() {
        return useGlobalResolver;
    }

    public void setUseGlobalResolver(boolean useGlobalResolver) {
        this.useGlobalResolver = useGlobalResolver;
    }

    public FailureStrategy getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(FailureStrategy failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    @Override
    public String toString() {
        return "EasyDesensitizeProperties{" +
                "enabled=" + enabled +
                ", useGlobalCache=" + useGlobalCache +
                ", useGlobalResolver=" + useGlobalResolver +
                ", failureStrategy=" + failureStrategy +
                '}';
    }
}
