package cn.procsl.ping.boot.user.domain.dictionary.model;

import cn.procsl.ping.boot.domain.annotation.CreateRepository;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.utils.StringUtils;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * 数据字典
 * 数据字典为一种树结构, 通过指定的分割符可以实现命名空间策略
 *
 * @author procsl
 * @date 2020年8月23日
 */
@Setter(AccessLevel.PROTECTED) // for jpa
@Getter
@EqualsAndHashCode
@ToString(exclude = {"path", "payload"})
@Table
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@Slf4j
@EntityListeners(DomainEventListener.class)
@CreateRepository
public class Dictionary implements AdjacencyNode<Long, DictPath> {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generator")
    @SequenceGenerator(allocationSize = 100, name = "generator")
    @Access(AccessType.PROPERTY)
    protected Long id;

    @Column(length = SPACE_NAME_LEN, nullable = false)
    protected String space;

    @Column(nullable = false)
    protected Long parentId;

    @Column(nullable = false)
    protected Integer depth;

    @Column(nullable = false)
    @Setter(AccessLevel.PUBLIC)
    protected Boolean active;

    @ElementCollection
    @Setter(AccessLevel.PUBLIC)
    protected List<Payload> payload;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    protected Set<DictPath> path;

    @Transient
    static Supplier<String> delimiter = () -> "/";

    public static final int SPACE_NAME_LEN = 20;

    public static final int ROOT_DEPTH = 0;

    @Transient
    private DictPath currentNode;

    public Dictionary(@NonNull String nameSpace, Payload... payloads) {
        this.empty();
        this.rename(nameSpace);
        this.setActive(true);
    }

    public Dictionary(@NonNull String space, @NonNull Dictionary parent, Payload... payloads) {
        assert parent.getId() != null;
        this.rename(space);
        this.setActive(parent.getActive());
        this.changeParent(parent);
    }


    @Transient
    public static List<String> split(@NonNull String path) {
        return Arrays.
            stream(path.split(delimiter.get()))
            .filter(item -> !StringUtils.isEmpty(item))
            .collect(Collectors.toUnmodifiableList());
    }

    public void setId(Long id) {
        if (this.getParentId() == null) {
            this.parentId = id;
        }
    }

    @Override
    public DictPath currentPathNode() {
        if (this.currentNode == null) {
            this.currentNode = new DictPath(this.getId(), this.getDepth(), this.getSpace());
        }
        return this.currentNode;
    }

    /**
     * 修改父节点
     *
     * @param parent 指定的父节点
     */
    @Override
    public void changeParent(AdjacencyNode<Long, DictPath> parent) {
        log.info("修改当前节点的父节点");
        if (!(parent instanceof Dictionary)) {
            throw new IllegalArgumentException("required " + this.getClass().getName());
        }
        this.empty();
        this.setParentId(parent.getId());
        log.trace("修改parentId:{}", this.getParentId());

        this.setDepth(parent.getDepth() + 1);
        log.trace("修改depth:{}", this.getDepth());

        this.getPath().addAll(parent.getPath());
        log.trace("添加父节点的path:{}", this.getPath());

        this.getPath().add(parent.currentPathNode());
        this.upgrade();
    }

    /**
     * 更新自身信息
     */
    protected void upgrade() {

        log.trace("开始更新自身");
        if (this.getId() == null) {
            log.trace("当前ID不存在, 放弃更新自身");
            return;
        }

        // 添加当前节点
        if (this.getParentId() == null) {
            log.trace("设置当前的id为parentId:{}", this.getId());
            this.setParentId(this.getId());
        }

        this.getPath().add(this.currentPathNode());
    }

    void empty() {
        // 如果为root则parentId == id
        log.trace("当前实体被置空");
        log.trace("设置parent=id:{}", id);
        this.setParentId(id);
        log.trace("设置depth:{}", id);
        this.setDepth(0);
        if (this.getPath() == null) {
            log.trace("初始化一个空的path");
            this.setPath(new HashSet<>());
        } else {
            log.trace("清空path");
            this.getPath().clear();
        }

    }

    /**
     * 当新对象被加入持久化上下文时回调方法
     */
    @Override
    public void postPersist() {
        this.upgrade();
    }

    /**
     * 重命名当前的节点
     *
     * @param spaceName 节点新名称
     */
    public void rename(String spaceName) {
        log.info("设置节点:{}名称:{}", this.getId(), spaceName);
        if (spaceName.contains(Dictionary.delimiter.get())) {
            throw new IllegalArgumentException("space存在非法字符(用于分隔符):" + delimiter.get());
        }
        this.setSpace(spaceName);
    }

}
