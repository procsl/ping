package cn.procsl.ping.boot.user.domain.common.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import lombok.NonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractTree<ID extends Serializable, T extends AdjacencyPathNode<ID>> implements AdjacencyNode<ID, T> {


    protected abstract void setId(ID id);

    protected abstract void setPath(Set<T> path);

    protected abstract void setDepth(Integer depth);

    protected abstract void setParentId(ID parentId);


    /**
     * 修改父节点
     *
     * @param parent 指定的父节点
     */
    @Override
    public void changeParent(AdjacencyNode<ID, T> parent) {
        this.empty();
        if (parent == this || parent == null) {
            return;
        }
        @NonNull
        ID pid = parent.getId();
        this.setParentId(pid);
        this.setDepth(parent.getDepth() + 1);
        this.getPath().addAll(parent.getPath());
        this.upgrade();
    }

    private void upgrade() {
        if (this.getId() == null) {
            return;
        }

        if (this.getParentId() == null) {
            this.setParentId(this.getId());
        }

        // 添加当前节点
        this.getPath().add(this.currentPathNode());
    }

    private void empty() {
        this.setParentId(this.getId());
        this.setDepth(0);
        if (this.getPath() == null) {
            this.setPath(new HashSet<>());
        } else {
            this.getPath().clear();
        }

    }

    /**
     * 当新对象被加入持久化上下文时回调方法
     */
    @Override
    public void postPersist() {
        this.upgrade();
    }
}
