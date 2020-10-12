package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.apt.annotation.RepositoryCreator;
import cn.procsl.ping.boot.domain.business.state.model.BooleanStateful;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Setter(AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(exclude = {"subject", "permission"})
@ToString(exclude = {"subject", "permission"})
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Entity(name = "$user:role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@EntityListeners(DomainEventListener.class)
@RepositoryCreator
public class Role implements BooleanStateful<Long>, DomainEntity<Long> {

    public final static int ROLE_NAME_LEN = 20;

    public final static String DELIMITER = "/";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_sequences")
    @Access(AccessType.PROPERTY)
    protected Long id;

    @ManyToMany
    @JoinTable(name = "$user:subject_role")
    protected Set<Subject> subject;

    @JoinTable(name = "$user:role_permission")
    @ManyToMany
    protected Set<Permission> permission;

    @Column(length = ROLE_NAME_LEN, nullable = false)
    @Setter(AccessLevel.PUBLIC)
    protected String name;

    @Column(nullable = false)
    @Setter(AccessLevel.PUBLIC)
    protected Boolean state;

    public Role(@NonNull String name, Collection<Permission> perms) {
        this.name = name;
        this.permission = CollectionUtils.createAndAppend(this.permission, perms);
        this.enable();
    }

    public void changePermissions(Collection<Permission> permissions) {
        CollectionUtils.nullSafeClear(this.permission);
        this.permission = CollectionUtils.createAndAppend(this.permission, permissions);
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

    @Override
    public boolean isEnable() {
        return state;
    }

}

