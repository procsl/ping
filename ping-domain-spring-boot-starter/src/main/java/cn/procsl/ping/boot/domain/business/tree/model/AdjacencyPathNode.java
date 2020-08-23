package cn.procsl.ping.boot.domain.business.tree.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
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
     * 不要设置此值的字段
     *
     * @return
     */
    @Deprecated
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
    Integer getSeq();

}
