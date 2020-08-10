package cn.procsl.ping.boot.domain.business.entity;

import java.io.Serializable;

/**
 * 邻接表关系表
 *
 * @author procsl
 * @date 2020/07/29
 */
public interface AdjacencyPathNode<ID extends Serializable> extends Serializable {

    /**
     * 路径树 父节点ID
     *
     * @return
     */
    ID getPathId();

    /**
     * 当前节点相对于root节点id的序号, root节点为seq为0
     *
     * @return
     */
    Integer getSeq();

}
