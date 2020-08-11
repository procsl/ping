package cn.procsl.ping.boot.domain.domain.entity;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyPathNode;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.OrderBy;

/**
 * @author procsl
 * @date 2020/07/31
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Embeddable
public class PathNode implements AdjacencyPathNode<String>, DomainEntity {

    @Column(length = UUID_2_LENGTH)
    String pathId;

    @OrderBy("asc")
    Integer seq;

    @Override
    @Column(length = UUID_2_LENGTH, updatable = false, insertable = false)
    public String getId() {
        return null;
    }
}
