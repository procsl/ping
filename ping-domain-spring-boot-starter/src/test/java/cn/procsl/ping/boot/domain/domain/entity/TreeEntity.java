package cn.procsl.ping.boot.domain.domain.entity;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.support.exector.DomainEventListener;
import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author procsl
 * @date 2020/07/31
 */
@Data
@Entity
@Table
@EntityListeners(DomainEventListener.class)
public class TreeEntity implements AdjacencyNode<Long, PathNode> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "general")
    @SequenceGenerator(allocationSize = 500, name = "general")
    @Column(updatable = false, nullable = false)
    Long id;

    Long parentId;

    @Lob
    String name;

    Integer depth;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    Set<PathNode> path;

    @Override
    public PathNode createPathNode(Long pathId, Integer seq) {
        return new PathNode(pathId, seq);
    }

    @Override
    public void changeParent(AdjacencyNode<Long, PathNode> parent) {
        if (!(parent instanceof TreeEntity)) {
            throw new IllegalArgumentException("required " + this.getClass().getName());
        }
        this.empty();
        this.parentId = parent.getId();
        this.depth = ((TreeEntity) parent).depth + 1;
        // 添加父节点的路径
        this.path.addAll(parent.getPath());
        // 添加父节点
        this.path.add(this.createPathNode(parentId, parent.getDepth()));
        // 把当前的子节点更新进去, 在合适的时机
        this.selfUpdate();
    }

    void empty() {
        // 如果为root则parentId == id
        this.parentId = id;
        this.depth = 0;
        if (path == null) {
            this.path = new HashSet<>();
        } else {
            this.path.clear();
        }
    }

    /**
     * 创建当前的节点, 并且填充
     */
    @Override
    public void postPersist() {
        selfUpdate();
    }

    public void init() {
        empty();
    }

    void selfUpdate() {
        if (id == null) {
            return;
        }

        // 添加当前节点
        if (this.parentId == null) {
            this.parentId = id;
        }

        this.path.add(this.createPathNode(this.id, this.depth));
        List<String> tmp = this.path
                .stream()
                .sorted((pre, next) -> pre.getSeq() - next.getSeq())
                .map(item -> String.valueOf(item.getPathId())).collect(Collectors.toList());
        this.setName("/" + String.join("/", tmp));
    }
}
