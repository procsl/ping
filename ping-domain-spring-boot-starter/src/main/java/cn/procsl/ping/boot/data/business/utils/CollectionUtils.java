package cn.procsl.ping.boot.data.business.utils;

import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author procsl
 * @date 2020/04/15
 */
@NoArgsConstructor(access = PRIVATE)
public class CollectionUtils {

    /**
     * 如果传入的collects 不支持写则抛出异常
     *
     * @param collects 填充的set 可以为null
     * @param elements 待填充的元素
     * @param <T>      元素类型
     * @return 填充后的元素
     * @throws UnsupportedOperationException 对于不支持写的操作抛出此异常
     */
    @Nullable
    @SafeVarargs
    public static <T> Set<T> createAndAppend(@Nullable Set<T> collects, @Nullable T... elements) throws UnsupportedOperationException {

        if (elements == null || elements.length == 0) {
            return collects;
        }

        if (collects == null) {
            collects = new HashSet<>();
        }

        Collections.addAll(collects, elements);

        return collects;
    }

    /**
     * 创建或添加元素 并返回Set容器
     *
     * @param collects 可读写的Set
     * @param elements 元素
     * @param <T>      泛型类型
     * @return 返回旧容器或在容器不存在的情况下创建并添加元素的容器
     * @throws UnsupportedOperationException 对于不支持写的操作抛出此异常
     */
    @Nullable
    public static <T> Set<T> createAndAppend(@Nullable Set<T> collects, @Nullable Collection<T> elements) throws UnsupportedOperationException {
        if (isEmpty(elements)) {
            return collects;
        }

        if (collects == null) {
            collects = new HashSet<>();
        }

        collects.addAll(elements);

        return collects;
    }

    /**
     * 空安全删除
     *
     * @param collection 容器,必须支持可写
     * @param elements   待删除的容器
     * @param <T>        泛型类型
     * @throws UnsupportedOperationException 对于不支持写的操作抛出此异常
     */
    @SafeVarargs
    public static <T> void nullSafeRemove(@Nullable Set<T> collection, @Nullable T... elements) throws UnsupportedOperationException {
        if (isEmpty(collection)) {
            return;
        }

        if (elements == null || elements.length == 0) {
            return;
        }

        for (T element : elements) {
            collection.remove(element);
        }
    }

    /**
     * 空安全删除元素
     *
     * @param collection 元素容器
     * @param elements   待删除容器
     * @param <T>        元素的泛型类型
     */
    public static <T> void nullSafeRemove(@Nullable Set<T> collection, @Nullable Collection<T> elements) throws UnsupportedOperationException {
        if (isEmpty(collection)) {
            return;
        }

        if (isEmpty(elements)) {
            return;
        }

        collection.removeAll(elements);
    }

    /**
     * 测试元素是否存在容器中
     *
     * @param collection 容器
     * @param element    元素
     * @param <T>        元素泛型
     * @return 返回测试结果
     */
    public static <T> boolean nullSafeContains(@Nullable Collection<T> collection, @Nullable T element) {
        if (element == null) {
            return false;
        }

        if (isEmpty(collection)) {
            return false;
        }

        return collection.contains(element);
    }

    /**
     * 检测是否为null或empty
     *
     * @param collection 指定的集合
     * @return 为空返回true
     */
    public static boolean isEmpty(@Nullable Collection collection) {
        return (collection == null || collection.isEmpty());
    }
}
