package cn.procsl.ping.boot.user.domain.utils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
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
    @SafeVarargs
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

    /**
     * 创建或添加元素 并返回Set容器
     *
     * @param collects 可读写的Set
     * @param elements 元素
     * @param <T>      泛型类型
     * @return 返回旧容器或在容器不存在的情况下创建并添加元素的容器
     * @throws UnsupportedOperationException
     */
    @Nullable
    static <T> Set<T> createAndAppend(@Nullable Set<T> collects, @Nullable Collection<T> elements) throws UnsupportedOperationException {
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

    /**
     * 空安全删除
     *
     * @param collections 容器,必须支持可写
     * @param elements    待删除的容器
     * @param <T>         泛型类型
     * @throws UnsupportedOperationException
     */
    public static <T> void nullSafeRemove(@Nullable Set<T> collections, @Nullable T... elements) throws UnsupportedOperationException {
        if (collections == null || collections.isEmpty()) {
            return;
        }

        if (elements == null || elements.length == 0) {
            return;
        }

        for (T element : elements) {
            collections.remove(element);
        }
    }

    /**
     * 空安全删除元素
     *
     * @param collections 元素容器
     * @param elements    待删除容器
     * @param <T>
     */
    public static <T> void nullSafeRemove(@Nullable Set<T> collections, @Nullable Collection<T> elements)  throws UnsupportedOperationException{
        if (collections == null || collections.isEmpty()) {
            return;
        }

        if (elements == null || elements.isEmpty()) {
            return;
        }

        collections.removeAll(elements);
    }
}
