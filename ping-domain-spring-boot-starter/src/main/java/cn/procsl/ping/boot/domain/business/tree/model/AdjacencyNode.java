package cn.procsl.ping.boot.domain.business.tree.model;

import cn.procsl.ping.boot.domain.business.utils.ObjectUtils;
import cn.procsl.ping.business.domain.DomainEvents;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * 邻接表
 *
 * @author procsl
 * @date 2020/07/29
 */
@MappedSuperclass
public interface AdjacencyNode<ID extends Serializable, T extends AdjacencyPathNode<ID>> extends DomainEvents<ID> {

    int ROOT_DEPTH = 0;

    @Override
    @Id
    ID getId();

    ID getParentId();

    Integer getDepth();

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    Set<T> getPath();

    @Transient
    default boolean isRoot() {
        return ObjectUtils.nullSafeEquals(getId(), getParentId()) || ObjectUtils.nullSafeEquals(getDepth(), 0);
    }

    /**
     * 创建路径节点实例方法
     *
     * @return 返回当前节点的 DictPath
     */
    T currentPathNode();

    /**
     * 修改父节点
     *
     * @param parent 指定的父节点
     */
    void changeParent(AdjacencyNode<ID, T> parent);
}
