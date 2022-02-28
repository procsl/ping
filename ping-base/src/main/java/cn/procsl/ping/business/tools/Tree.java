package cn.procsl.ping.business.tools;

import java.io.Serializable;
import java.util.List;

/**
 * 树
 *
 * @author procsl
 * @date 2020/07/23
 */
public interface Tree<K extends Serializable, V> {

    /**
     * 判单当前的树是否为空树
     *
     * @return 空树返回 true
     */
    boolean isEmpty();

    /**
     * 判断是否为根节点
     *
     * @return 根节点返回true
     */
    boolean isRoot();

    /**
     * 是否存在key
     *
     * @param key 指定的key
     * @return 如果存在返回true
     */
    boolean contains(K key);

    /**
     * 获取深度
     *
     * @return 当前节点的深度, root节点的深度为0
     */
    int depth();

    /**
     * 清理当前树节点的所有子节点
     */
    void clear();

    /**
     * 为当前的树节点添加子树
     * 子树的key在当前的节点下必须唯一, 否则会替换
     *
     * @param tree 子树
     */
    void put(Tree<? extends K, ? extends V> tree);

    /**
     * 为当前的树节点添加子树, key在当前节点下必须唯一, 如果不唯一则会替换
     *
     * @param key   唯一key
     * @param value 绑定的值
     */
    void put(K key, V value);

    /**
     * 删除当前节点下的子节点
     *
     * @param key 指定的key
     */
    void remove(K key);

    /**
     * 删除当前节点下的子节点
     *
     * @param tree 指定的子节点
     */
    void remove(Tree<? extends K, ? extends V> tree);

    /**
     * 以当前节点的父节点为基准返回路径
     * 比如: /path
     *
     * @return 当前节点路径
     */
    String path();

    /**
     * 以当前的节点为基准返回指定分割符的路径
     * 例如:
     * <p>
     * ;path
     * \\path
     * |path
     *
     * @param delimiter 分割符
     * @return 返回路径
     */
    String path(String delimiter);

    /**
     * 从当前节点的子节点下搜索指定key的节点
     *
     * @param key 用于匹配的key
     * @return 返回搜索到的节点
     */
    Tree<K, V> search(K key);

    /**
     * 递归搜索
     *
     * @param key 指定的key
     * @return 返回搜索到的节点列表
     */
    List<Tree<K, V>> fullSearch(K key);

    /**
     * 返回当前树的根节点
     *
     * @return 根节点对象
     */
    Tree<K, V> root();

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    Tree<K, V> parent();

    /**
     * 返回当前节点的子节点列表
     *
     * @return 子节点列表
     */
    List<Tree<K, V>> child();

}
