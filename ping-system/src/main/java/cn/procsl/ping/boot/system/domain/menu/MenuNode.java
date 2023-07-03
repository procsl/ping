package cn.procsl.ping.boot.system.domain.menu;

import cn.procsl.ping.boot.jpa.domain.tree.AdjacencyPathNode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Setter
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuNode implements AdjacencyPathNode<Long> {

    Long id;

    @Column(updatable = false)
    Long pathId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuNode menuNode = (MenuNode) o;

        if (!getId().equals(menuNode.getId())) return false;
        return getPathId().equals(menuNode.getPathId());
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getPathId().hashCode();
        return result;
    }
}
