package cn.procsl.ping.boot.domain.business.tree.model;

import cn.procsl.ping.boot.domain.business.utils.ObjectUtils;
import cn.procsl.ping.boot.domain.support.exector.DomainEventListener;
import cn.procsl.ping.business.domain.DomainEntity;
import cn.procsl.ping.business.domain.DomainEvents;
import org.hibernate.annotations.DynamicUpdate;

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
@EntityListeners(DomainEventListener.class)
@DynamicUpdate
public interface AdjacencyNode<ID extends Serializable, T extends AdjacencyPathNode<ID>>
        extends DomainEntity, DomainEvents {

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
     * @param pathId
     * @param seq
     * @return
     */
    T createPathNode(ID pathId, Integer seq);

    /**
     * 修改父节点
     *
     * @param parent 指定的父节点
     */
    void changeParent(AdjacencyNode<ID, T> parent);
}
