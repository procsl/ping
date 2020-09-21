package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.domain.annotation.CreateRepository;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import cn.procsl.ping.boot.user.domain.common.AbstractTree;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Set;

@Setter(AccessLevel.PROTECTED) // for jpa
@Getter
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = "roles")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Entity(name = "r_perm")
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@Slf4j
@EntityListeners(DomainEventListener.class)
@CreateRepository
public class Permission extends AbstractTree<Long, Node> implements DomainEntity<Long> {

    final public static int PERM_NAME_LEN = 20;

    final public static int PERM_TYPE_LEN = 10;

    final public static int PERM_TARGET_LEN = 10;

    final public static String DELIMITER = "/";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generator")
    @Access(AccessType.PROPERTY)
    Long id;

    @Column(length = PERM_NAME_LEN, nullable = false)
    @Setter(AccessLevel.PUBLIC)
    String name;

    @Column(length = PERM_TYPE_LEN, nullable = false)
    String type;

    @Column(nullable = false)
    Integer depth;

    @Column(nullable = false)
    Long parentId;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    Set<Node> path;

    @ManyToMany
    Set<Role> roles;

    @Column(length = PERM_TYPE_LEN, nullable = false)
    String target;

    /**
     * 操作符
     * 读 写 执行, 只读,等等 可自定义
     */
    @Embedded
    Operator operator;


    public Permission(
        @NonNull String name,
        @NonNull String type,
        @NonNull String target,
        @NonNull Operator operator,
        Permission parent
    ) {
        this.name = name;
        this.type = type;
        this.target = target;
        this.operator = operator;
        this.changeParent(parent);
    }

    /**
     * 创建路径节点实例方法
     *
     * @return 返回当前节点的 DictPath
     */
    @Override
    public Node currentPathNode() {
        return new Node(this.parentId, this.depth);
    }

    /**
     * 查找分隔符
     *
     * @return
     */
    @Override
    public String findDelimiter() {
        return DELIMITER;
    }
}
