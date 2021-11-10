package cn.procsl.ping.boot.domain.support.utils;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    private ReflectionUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取指定实例的属性
     *
     * @param target    目标对象
     * @param instance  指定的实例
     * @param filedName 属性名称
     * @param type      属性类型
     * @param <T>       属性范型
     * @return
     * @throws NoSuchFieldException   如果实例未找到则抛出此异常
     * @throws IllegalAccessException 访问失败异常
     */
    public static <T> T findField(Class target, Object instance, String filedName, Class<T> type) {
        Field field = org.springframework.util.ReflectionUtils.findField(target, filedName, type);
        if (field == null) {
            throw new IllegalArgumentException("not found " + filedName + " by " + instance.getClass().getName());
        }
        try {
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            T tmp = (T) field.get(instance);
            field.setAccessible(false);
            return tmp;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("accessible error", e);
        }
    }
}
