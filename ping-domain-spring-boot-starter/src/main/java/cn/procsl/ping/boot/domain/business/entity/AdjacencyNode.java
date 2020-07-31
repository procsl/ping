package cn.procsl.ping.boot.domain.business.entity;

import java.io.Serializable;
import java.util.Set;

/**
 * 邻接表
 *
 * @author procsl
 * @date 2020/07/29
 */
public interface AdjacencyNode<ID extends Serializable> extends Serializable {

    ID getId();

    ID getParentId();

    Integer getDepth();

    Set<AdjacencyPathNode<ID>> getPath();
}
