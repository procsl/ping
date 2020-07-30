package cn.procsl.ping.boot.data.business.entity;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Set;

/**
 * 邻接表
 *
 * @author procsl
 * @date 2020/07/29
 */
@MappedSuperclass
@Embeddable
public interface AdjacencyNode<ID extends Serializable> extends Serializable {

    ID getId();

    ID getParentId();

    Integer getDepth();

    Set<AdjacencyPathNode<ID>> getPath();
}
