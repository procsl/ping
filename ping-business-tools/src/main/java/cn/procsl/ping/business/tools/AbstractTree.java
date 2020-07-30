package cn.procsl.ping.business.tools;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * @author procsl
 * @date 2020/07/28
 */
public abstract class AbstractTree<K extends Serializable, V> implements Tree<K, V> {

    @Override
    public boolean isRoot() {
        return this.parent() == null;
    }

    @Override
    public int depth() {
        if (this.parent() == null) {
            return 0;
        }
        return this.parent().depth() + 1;
    }

    @Override
    public List<Tree<K, V>> fullSearch(K key) {

        List<Tree<K, V>> child = this.child();
        if (child == null) {
            return EMPTY_LIST;
        }

        // 从一级层面搜索
        Tree<K, V> tmp = this.search(key);

        List<Tree<K, V>> list = new LinkedList<>();
        list.add(tmp);
        for (Tree<K, V> tree : child) {
            list.addAll(tree.fullSearch(key));
        }
        // 拉取所有的子节点, 逐一遍历
        return list;
    }

    @Override
    public Tree<K, V> root() {
        if (isRoot()) {
            return this;
        }
        return this.parent().root();
    }

    @Override
    public boolean isEmpty() {
        List<Tree<K, V>> childs = this.child();
        return (childs == null || childs.isEmpty());
    }
}
