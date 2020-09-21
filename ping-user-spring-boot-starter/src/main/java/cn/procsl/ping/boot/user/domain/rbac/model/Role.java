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
@EqualsAndHashCode(exclude = {"path", "users", "perms"})
@ToString(exclude = {"path", "users", "perms"})
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Entity(name = "r_role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@EntityListeners(DomainEventListener.class)
@CreateRepository
public class Role extends AbstractTree<Long, Node> implements BooleanStateful<Long> {

    public final static int ROLE_NAME_LEN = 20;

    public final static String DELIMITER = "/";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generator")
    protected Long id;

    @ManyToMany
    protected Set<Subject> users;

    @ManyToMany
    @Setter(AccessLevel.PUBLIC)
    protected Set<Permission> perms;

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
        if (parent != null) {
            this.changeParent(parent);
        }
        this.perms = CollectionUtils.createAndAppend(this.perms, perms);
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
        CollectionUtils.nullSafeClear(this.perms);
        this.perms = CollectionUtils.createAndAppend(this.perms, permissions);
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
}

