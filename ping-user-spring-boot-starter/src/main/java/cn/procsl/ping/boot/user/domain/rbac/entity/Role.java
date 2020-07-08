package cn.procsl.ping.boot.user.domain.rbac.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.business.entity.GeneralEntity;
import cn.procsl.ping.boot.user.utils.CollectionUtils;
import cn.procsl.ping.boot.user.utils.ObjectUtils;
import cn.procsl.ping.boot.user.utils.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

/**
 * 用户角色
 *
 * @author procsl
 * @date 2020/04/06
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@NoArgsConstructor(access = PRIVATE)
@Setter(PRIVATE)
@CreateRepository
public class Role extends GeneralEntity {

    public final static String ROLE_ID_NAME = "role_id";

    public final static int NAME_LENGTH = 50;

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(length = NAME_LENGTH, nullable = false)
    protected String name;

    protected Role inherit;

    @ElementCollection
    protected Set<Permission> permissions;

    protected Role(String name) {
        this.changeName(name);
    }

    protected Role(String name, Role inherit) {
        this(name);
        this.changeInherit(inherit);
    }

    protected Role(String name, Role inherit, Collection<Permission> permissions) {
        this(name, inherit);
        this.permissions = CollectionUtils.createAndAppend(this.permissions, permissions);
    }

    public static Role create(String name) {
        return new Role(name);
    }

    public static Role create(String name, Role inherit) {
        return new Role(name, inherit);
    }

    public static Role create(String name, Role inherit, Collection<Permission> permissions) {
        return new Role(name, inherit, permissions);
    }

    /**
     * 设置继承的角色
     *
     * @param parent 父节点ID
     */
    public void changeInherit(@Nullable Role parent) {
        if (parent == null || ObjectUtils.nullSafeEquals(parent.id, id)) {
            this.inherit = null;
        }
        this.inherit = parent;
    }

    /**
     * 重命名角色名称
     *
     * @param name 新的角色名称
     */
    public void changeName(@NonNull String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("角色名称不可为空");
        }

        if (name.length() > NAME_LENGTH) {
            throw new IllegalArgumentException("错误的角色名称");
        }
        this.name = name;
    }

    public void grantPermission(@NonNull Permission permission) {
        this.permissions = CollectionUtils.createAndAppend(this.permissions, permission);
    }

    public void revokePermission(@NonNull Permission permission) {
        CollectionUtils.nullSafeRemove(this.permissions, permission);
    }

    public boolean hasPermission(@NonNull Permission permission) {
        return CollectionUtils.nullSafeContains(this.permissions, permission);
    }
}

