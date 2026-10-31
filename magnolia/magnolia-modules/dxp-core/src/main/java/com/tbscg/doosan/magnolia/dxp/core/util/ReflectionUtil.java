package com.tbscg.doosan.magnolia.dxp.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil<T> {

    public static <T> T invoke(Method method, Object instance, Object... parameters) {
        try {
            method.setAccessible(true);
            return (T) method.invoke(instance, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(Class<?> declaredClass, String name, Class<?>... parameterTypes) {
        try {
            return declaredClass.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
