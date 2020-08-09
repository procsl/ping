package cn.procsl.ping.boot.domain.domain.entity;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyPathNode;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author procsl
 * @date 2020/07/31
 */
@Data
@Embeddable
public class PathNode implements AdjacencyPathNode<String>, DomainEntity {

    @Column(length = UUID_LENGTH)
    String pathId;

    Integer seq;
}
