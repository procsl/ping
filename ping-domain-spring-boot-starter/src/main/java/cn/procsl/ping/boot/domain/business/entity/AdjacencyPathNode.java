package cn.procsl.ping.boot.domain.business.entity;

import java.io.Serializable;

/**
 * 邻接表关系表
 *
 * @author procsl
 * @date 2020/07/29
 */
public interface AdjacencyPathNode<ID extends Serializable> extends Serializable {

    ID getPathId();

    Integer getSeq();
}
