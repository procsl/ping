package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.domain.annotation.CreateRepository;
import cn.procsl.ping.boot.domain.business.state.model.BooleanStateful;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import cn.procsl.ping.boot.user.domain.common.AbstractTree;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Setter(AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(exclude = {"path", "subject", "permission"})
@ToString(exclude = {"path", "subject", "permission"})
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "depth", "parentId"})})
@Entity(name = "${User.Role}")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@EntityListeners(DomainEventListener.class)
@CreateRepository
public class Role extends AbstractTree<Long, Node> implements BooleanStateful<Long> {

    public final static int ROLE_NAME_LEN = 20;

    public final static String DELIMITER = "/";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_sequences")
    @Access(AccessType.PROPERTY)
    protected Long id;

    @ManyToMany
    @JoinTable(name = "${User.subject_role}")
    protected Set<Subject> subject;

    @JoinTable(name = "${User.role_permission}")
    @ManyToMany
    @Setter(AccessLevel.PUBLIC)
    protected Set<Permission> permission;

    @Column(length = ROLE_NAME_LEN, nullable = false)
    @Setter(AccessLevel.PUBLIC)
    protected String name;

    @Column(nullable = false)
    protected Long parentId;

    @Column(nullable = false)
    protected Integer depth;

    @Column(nullable = false)
    @Setter(AccessLevel.PUBLIC)
    protected Boolean state;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    @Setter(AccessLevel.PUBLIC)
    protected Set<Node> path;

    public Role(@NonNull String name, Role parent, Collection<Permission> perms) {
        this.name = name;
        this.changeParent(parent);
        this.permission = CollectionUtils.createAndAppend(this.permission, perms);
        this.enable();
    }

    @Override
    protected void setId(Long id) {
        this.id = id;
        if (parentId == null) {
            this.parentId = id;
        }
    }

    @Override
    public Node currentPathNode() {
        return new Node(this.id, this.depth);
    }

    public void changePermissions(Collection<Permission> permissions) {
        CollectionUtils.nullSafeClear(this.permission);
        this.permission = CollectionUtils.createAndAppend(this.permission, permissions);
    }

    /**
     * 查找分隔符
     *
     * @return 分隔符
     */
    @Override
    public String findDelimiter() {
        return DELIMITER;
    }

    /**
     * 判断是否存在权限
     *
     * @param target 指定的target
     * @param <T>    target范型
     * @return 如果存在, 则返回true
     */
    public <T extends Target> boolean hasPermission(T target) {
        if (target == null) {
            return false;
        }

        if (CollectionUtils.isEmpty(this.permission)) {
            return false;
        }

        if (!(target instanceof Permission)) {
            return equals(target);
        }

        if (((Permission) target).getId() != null) {
            return this.permission.contains(target);
        }

        return equals(target);
    }

    private boolean equals(Target target) {
        for (Permission p : this.permission) {
            if (target.isEquals(p)) {
                return true;
            }
        }
        return false;
    }
}

