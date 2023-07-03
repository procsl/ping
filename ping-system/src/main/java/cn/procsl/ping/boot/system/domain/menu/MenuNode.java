package cn.procsl.ping.boot.system.domain.menu;

import cn.procsl.ping.boot.jpa.domain.tree.AdjacencyPathNode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Setter
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuNode implements AdjacencyPathNode<Long> {

    @Column(updatable = false, nullable = false)
    Long currentId;

    @Column(updatable = false, nullable = false)
    Long parentId;

    public MenuNode(@NonNull Long currentId, @NonNull Long parentId) {
        this.currentId = currentId;
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "{" +
                "currentId=" + currentId +
                ", parentId=" + parentId +
                '}';
    }
}
