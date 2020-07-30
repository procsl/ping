package cn.procsl.ping.boot.data.business.entity;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * 邻接表关系表
 *
 * @author procsl
 * @date 2020/07/29
 */
@Embeddable
@MappedSuperclass
public interface AdjacencyPathNode<ID extends Serializable> extends Serializable {

    ID getPathId();

    Integer getSeq();
}
