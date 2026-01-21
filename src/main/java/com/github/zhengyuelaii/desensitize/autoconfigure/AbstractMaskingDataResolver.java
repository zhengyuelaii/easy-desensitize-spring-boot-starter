package com.github.zhengyuelaii.desensitize.autoconfigure;

import io.github.zhengyuelaii.desensitize.core.util.MaskingDataResolver;

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
            // 1. 如果是普通类 (如 String)
            if (actual instanceof Class) {
                return (Class<?>) actual;
            }

            // 2. 如果是带泛型的类 (如 Result<?>)
            if (actual instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) actual).getRawType();
            }
        }
        throw new IllegalStateException("Cannot resolve generic Type T for " + getClass().getName());
    }

}
