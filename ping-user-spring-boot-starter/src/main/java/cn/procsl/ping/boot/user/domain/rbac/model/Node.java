package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@EqualsAndHashCode(exclude = "seq")
@Data
@Embeddable
@Immutable
@NoArgsConstructor
public class Node implements AdjacencyPathNode<Long> {

    @Column(nullable = false, updatable = false)
    protected Long pathId;

    @Column(nullable = false, updatable = false)
    protected Integer seq;

    public Node(@NonNull Long pathId, @NonNull Integer seq) {
        this.pathId = pathId;
        this.seq = seq;
    }
}
