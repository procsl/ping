package cn.procsl.ping.boot.jpa.domain.tree;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;

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
    @Column(updatable = false, insertable = false)
    ID getCurrentId();


    void setCurrentId(ID id);

    /**
     * 路径树 父节点ID
     *
     * @return parent id
     */
    ID getParentId();

    void setParentId(ID id);

}
