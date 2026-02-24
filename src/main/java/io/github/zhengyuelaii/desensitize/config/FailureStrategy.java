package io.github.zhengyuelaii.desensitize.config;

public enum FailureStrategy {
    FAIL_OPEN,     // 失败放行（当前行为）
    FAIL_CLOSE,    // 失败抛异常
}
