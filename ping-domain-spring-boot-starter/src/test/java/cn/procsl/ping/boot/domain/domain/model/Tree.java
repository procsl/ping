package cn.procsl.ping.boot.domain.domain.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@ToString(exclude = "path")
@EqualsAndHashCode
@Entity
@Table
@EntityListeners(DomainEventListener.class)
@DynamicUpdate
@Slf4j
public class Tree implements AdjacencyNode<Long, PathNode> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @SequenceGenerator(allocationSize = 500, name = "generator", sequenceName = "tree_entity_seq")
    @Column(updatable = false, nullable = false)
    Long id;

    Long parentId;

    @Lob
    String name;

    Integer test = 0;

    Integer test1 = 1;

    Integer test2 = 2;

    Integer test3 = 3;

    Integer test4 = 4;

    Integer depth;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    Set<PathNode> path;


    /**
     * 创建路径节点实例方法
     *
     * @return
     */
    @Override
    public PathNode currentPathNode() {
        return new PathNode(this.id, this.depth);
    }

    @Override
    public void changeParent(AdjacencyNode<Long, PathNode> parent) {
        log.info("修改当前节点的父节点");
        if (!(parent instanceof cn.procsl.ping.boot.domain.domain.model.Tree)) {
            throw new IllegalArgumentException("required " + this.getClass().getName());
        }
        this.empty();

        this.parentId = parent.getId();
        log.debug("修改parentId:{}", parentId);

        this.depth = ((cn.procsl.ping.boot.domain.domain.model.Tree) parent).depth + 1;
        log.debug("修改depth:{}", depth);

        this.path.addAll(parent.getPath());
        log.debug("添加父节点的path:{}", this.path);

        this.path.add(parent.currentPathNode());

        this.selfUpdate();
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

    public void init() {
        log.debug("初始化当前实例");
        empty();
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

        PathNode pathNode = new PathNode(id, depth);
        log.debug("添加当前的节点至PathNodes:{}", pathNode);
        this.path.add(pathNode);
        List<String> tmp = this.path
            .stream()
            .sorted(Comparator.comparingInt(PathNode::getSeq))
            .map(item -> String.valueOf(item.getPathId())).collect(Collectors.toList());
        this.setName("/" + String.join("/", tmp));
        log.debug("设置当前的name属性为:{}", name);
    }
}
