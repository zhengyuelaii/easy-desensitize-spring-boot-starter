package com.github.zhengyuelaii.desensitize.autoconfigure;

import java.util.ArrayList;
import java.util.List;

/**
 * 脱敏数据解析器组合
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-19
 */
public class GlobalMaskingResolverComposite {

    private final List<AbstractMaskingDataResolver<?>> resolvers = new ArrayList<>();

    public void addResolver(AbstractMaskingDataResolver<?> resolver) {
        resolvers.add(resolver);
    }

    public void addResolvers(List<AbstractMaskingDataResolver<?>> resolvers) {
        this.resolvers.addAll(resolvers);
    }

    public Object resolve(Object source) {
        if (resolvers.isEmpty()) {
            return source;
        }

        for (AbstractMaskingDataResolver<?> resolver : resolvers) {
            try {
                if (resolver.supports(source)) {
                    Object resolvedBody = resolver.resolve(source);
                    // 递归处理解析后的结果
                    return resolve(resolvedBody);
                }
            } catch (Exception e) {
                continue;
            }
        }
        // 如果没有任何解析器匹配
        return source;
    }

}
