package cn.procsl.ping.boot.domain.domain.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Embeddable;

/**
 * 树节点
 *
 * @author procsl
 * @date 2020/07/31
 */
@NoArgsConstructor
@Data
@Embeddable
public class PathNode implements AdjacencyPathNode<Long> {

    Long pathId;

    Integer seq;

    public PathNode(@NonNull Long pathId, @NonNull Integer seq) {
        this.pathId = pathId;
        this.seq = seq;
    }


}
