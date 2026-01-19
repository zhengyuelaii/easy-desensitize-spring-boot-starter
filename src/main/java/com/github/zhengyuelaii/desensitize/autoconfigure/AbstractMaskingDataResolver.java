package com.github.zhengyuelaii.desensitize.autoconfigure;

import com.github.zhengyuelaii.desensitize.core.util.MaskingDataResolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 抽象脱敏数据解析器
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-19
 */
public abstract class AbstractMaskingDataResolver<T> implements MaskingDataResolver<T> {

    protected final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected AbstractMaskingDataResolver() {
        this.entityClass = (Class<T>) resolveEntityClass();
    }

    public boolean supports(Object source) {
        return entityClass.isInstance(source);
    }

    @Override
    public Object resolve(Object source) {
        return resolveInternal(entityClass.cast(source));
    }

    protected abstract Object resolveInternal(T source);

    private Class<?> resolveEntityClass() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type actual = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (actual instanceof Class) {
                return (Class<?>) actual;
            }
        }
        throw new IllegalStateException("Cannot resolve generic Type T for " + getClass().getName());
    }

}
