package cn.procsl.ping.boot.domain.domain.entity;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 树节点
 * @author procsl
 * @date 2020/07/31
 */
@NoArgsConstructor
@Data
@Embeddable
public class PathNode implements AdjacencyPathNode<String>, DomainEntity {

    @Column(length = UUID_2_LENGTH)
    String pathId;

    Integer seq;

    public PathNode(String pathId, Integer seq) {
        this.pathId = pathId;
        this.seq = seq;
    }


}
