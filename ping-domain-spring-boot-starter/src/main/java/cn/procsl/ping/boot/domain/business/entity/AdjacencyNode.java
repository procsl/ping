package cn.procsl.ping.boot.domain.business.entity;

import cn.procsl.ping.business.domain.DomainEntity;

import java.io.Serializable;
import java.util.Set;

/**
 * 邻接表
 *
 * @author procsl
 * @date 2020/07/29
 */
public interface AdjacencyNode<ID extends Serializable> extends Serializable, DomainEntity {

    ID getId();

    ID getParentId();

    Integer getDepth();

    Set<? extends AdjacencyPathNode<ID>> getPath();
}
