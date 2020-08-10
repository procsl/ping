package cn.procsl.ping.boot.domain.domain.entity;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.utils.StringUtils;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Collections;
import java.util.Set;

/**
 * @author procsl
 * @date 2020/07/31
 */
@Data
@Entity
@Table
public class TreeEntity implements AdjacencyNode<String, PathNode> {

    public static final TreeEntity root;
    public static final String DEFAULT_ROOT_NAME = "root";

    static {
        root = new TreeEntity() {
            @Override
            public boolean isRoot() {
                return true;
            }

            @Override
            public final void setId(String id) {
                if (this.id != null && this.id.isEmpty()) {
                    return;
                }
                this.id = id;
            }

            @Override
            public final void setParentId(String parentId) {
            }

            @Override
            public final void setDepth(Integer depth) {
            }

            @Override
            public final void setPath(Set<PathNode> path) {
            }

            @Override
            public final void fullByParent(AdjacencyNode<String, PathNode> parentNode) {
            }
        };
        root.name = DEFAULT_ROOT_NAME;
        root.depth = 0;
        root.path = Collections.emptySet();
    }

    @Id
    @Column(length = UUID_2_LENGTH)
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(name = "idGenerator", strategy = "uuid2")
    String id;

    @Column(length = UUID_2_LENGTH)
    String parentId;

    String name;

    Integer depth;

    @ElementCollection
    Set<PathNode> path;

    @Override
    public Set<PathNode> getPath() {
        return path;
    }

    @Override
    public void fullByParent(@NonNull AdjacencyNode<String, PathNode> parentNode) {
        @NonNull
        String id = parentNode.getId();
        this.parentId = id;

        Set<PathNode> pathNode = this.createPathBy(id);
        this.path = pathNode;

        depth = pathNode.size();
    }

    @Override
    public TreeEntity root() {
        return root;
    }

    @Override
    @Transient
    public boolean isRoot() {
        // 说明未持久化
        if (this.root() == null || StringUtils.isEmpty(this.root().id)) {
            // 检查 depth
            if (depth == null || depth == 0) {
                return true;
            }
        }
        // 如果ID相等, 则说明是root
        if (this.root().equals(id)) {
            return true;
        }
        return false;
    }

    @Override
    public PathNode createPathNode(String parentId, Integer seq) {
        return new PathNode(parentId, seq);
    }


}
