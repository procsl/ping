package cn.procsl.ping.boot.domain.business.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.OrderBy;
import java.io.Serializable;

/**
 * 邻接表关系表
 *
 * @author procsl
 * @date 2020/07/29
 */
@MappedSuperclass
public interface AdjacencyPathNode<ID extends Serializable> extends Serializable {

    /**
     * 绑定的treeId
     *
     * @return
     */
    @Column(updatable = false, insertable = false)
    default ID getId() {
        return null;
    }

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
    @OrderBy("asc")
    Integer getSeq();

}
