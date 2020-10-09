package cn.procsl.ping.boot.domain.domain.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author procsl
 * @date 2020/07/31
 */
@Setter
@Getter
@ToString(exclude = {"path"})
@EqualsAndHashCode(exclude = {"path"})
@Entity(name = "${Domain.Tree}")
@Table
@EntityListeners(DomainEventListener.class)
@DynamicUpdate
@Slf4j
@NoArgsConstructor
public class Tree implements AdjacencyNode<Long, Path> {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_sequences")
    @Access(AccessType.PROPERTY)
    Long id;

    Long parentId;

    @Lob
    String name;

    Integer depth;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    Set<Path> path;


    public Tree(Tree parent) {
        this.empty();
        changeParent(parent);
    }

    @Override
    public Path currentPathNode() {
        return new Path(this.id, this.depth);
    }

    void empty() {
        // 如果为root则parentId == id
        log.debug("当前实体被置空");
        log.debug("设置parent=id:{}", id);
        this.parentId = id;
        log.debug("设置depth:{}", id);
        this.depth = 0;
        if (path == null) {
            log.debug("初始化一个空的path");
            this.path = new HashSet<>();
        } else {
            log.debug("清空path");
            this.path.clear();
        }
    }

    /**
     * 创建当前的节点, 并且填充
     */
    @Override
    public void postPersist() {
        log.debug("保存前回调");
        selfUpdate();
    }

    @Override
    public void changeParent(AdjacencyNode<Long, Path> parent) {
        if (parent == null) {
            this.empty();
            return;
        }

        log.info("修改当前节点的父节点");
        if (!(parent instanceof Tree)) {
            throw new IllegalArgumentException("Required " + this.getClass().getName());
        }
        this.empty();

        this.parentId = parent.getId();
        log.debug("修改parentId:{}", parentId);

        this.depth = ((Tree) parent).depth + 1;
        log.debug("修改depth:{}", depth);

        this.path.addAll(parent.getPath());
        log.debug("添加父节点的path:{}", this.path);
        this.getPath().add(parent.currentPathNode());
        this.selfUpdate();
    }

    void selfUpdate() {
        log.debug("开始更新自身");
        if (id == null) {
            log.debug("当前ID不存在, 放弃更新自身");
            return;
        }

        // 添加当前节点
        if (this.parentId == null) {
            log.debug("设置当前的id为parentId:{}", id);
            this.parentId = id;
        }

        if (this.getId() == null) {
            return;
        }
        // 添加当前节点
        if (this.getParentId() == null) {
            this.setParentId(this.getId());
        }
        this.getPath().add(this.currentPathNode());

        List<String> tmp = this.path
            .stream()
            .sorted(Comparator.comparingInt(Path::getSeq))
            .map(item -> String.valueOf(item.getPathId())).collect(Collectors.toList());
        this.setName("/" + String.join("/", tmp));
        log.debug("设置当前的name属性为:{}", name);
    }

    public void setId(Long id) {
        this.id = id;
        if (this.parentId == null) {
            this.parentId = id;
        }
    }
}
