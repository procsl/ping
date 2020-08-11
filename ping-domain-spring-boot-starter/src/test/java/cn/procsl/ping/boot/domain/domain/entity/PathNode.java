package cn.procsl.ping.boot.domain.domain.entity;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyPathNode;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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

//    @OrderBy("seq ASC")
Integer seq;

    /**
     * 转换
     * @param pathNode
     * @return
     */
    public static PathNode convertTo(@NonNull AdjacencyPathNode<String> pathNode) {
        if (pathNode instanceof PathNode) {
            return (PathNode) pathNode;
        }

        @NonNull
        String pathId = pathNode.getPathId();

        @NonNull
        Integer seq = pathNode.getSeq();

        PathNode node = new PathNode(pathId, seq);
        return node;
    }
}
