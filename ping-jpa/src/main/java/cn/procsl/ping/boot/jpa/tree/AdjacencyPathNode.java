package cn.procsl.ping.boot.jpa.tree;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 邻接表关系表
 *
 * @author procsl
 * @date 2020/07/29
 */
@MappedSuperclass
@Embeddable
public interface AdjacencyPathNode<ID extends Serializable> extends Serializable {

    /**
     * 绑定的treeId
     * 不要设置此值的字段
     *
     * @return id
     */
    @Deprecated
    @Column(updatable = false, insertable = false)
    @Access(value = AccessType.PROPERTY)
    default ID getId() {
        return null;
    }


    default void setId(ID id) {
    }

    /**
     * 路径树 父节点ID
     *
     * @return parent id
     */
    ID getPathId();

    default void setPathId(ID id) {
    }

    /**
     * 当前节点相对于root节点id的序号, root节点为seq为0
     *
     * @return seq
     */
    Integer getSeq();

    default void setSeq(Integer seq) {
    }
}
