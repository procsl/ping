package cn.procsl.ping.boot.user.domain.utils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author procsl
 * @date 2020/04/15
 */
public class CollectionsUtils {

    /**
     * 如果传入的collects 不支持写则抛出异常
     *
     * @param collects 填充的set 可以为null
     * @param elements 待填充的元素
     * @param <T>      元素类型
     * @return 填充后的元素
     * @throws UnsupportedOperationException
     */
    @Nullable
    public static <T> Set<T> createAndAppend(@Nullable Set<T> collects, @Nullable T... elements) throws UnsupportedOperationException {

        if (elements == null || elements.length == 0) {
            return null;
        }

        if (collects == null) {
            collects = new HashSet<>();
        }

        for (T element : elements) {
            collects.add(element);
        }

        return collects;
    }

    @Nullable
    public static <T> Set<T> createAndAppend(@Nullable Set<T> collects, @Nullable List<T> elements) throws UnsupportedOperationException {
        if (elements == null || elements.isEmpty()) {
            return null;
        }

        if (collects == null) {
            collects = new HashSet<>();
        }

        for (T element : elements) {
            collects.add(element);
        }

        return collects;
    }
}
