package cn.procsl.ping.boot.user.domain.rbac.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.user.utils.CollectionUtils;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import static cn.procsl.ping.boot.data.business.entity.GeneralEntity.GENERAL_ENTITY_ID_LENGTH;
import static cn.procsl.ping.boot.user.domain.rbac.entity.Role.ROLE_ID_NAME;
import static lombok.AccessLevel.PRIVATE;

/**
 * 表示外部实体 机构 用户等需要角色权限管理的实体
 *
 * @author procsl
 */
@Entity
@Table
@NoArgsConstructor(access = PRIVATE)
@Description(comment = "用户身份聚合根")
@Getter
@Setter(PRIVATE)
@ToString
@CreateRepository
public class Session implements Serializable {

    public final static String SESSION_ID_NAME = "session_id";

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Description(comment = "启用/禁用")
    private boolean active;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {SESSION_ID_NAME, ROLE_ID_NAME}))
    @ElementCollection
    @Column(name = ROLE_ID_NAME)
    @Description(comment = "用户角色IDs")
    private Set<Long> roles;

    public Session(boolean active) {
        this.active = active;
    }

    public Session(boolean active, Collection<Long> roles) {
        this(active);
        this.roles = CollectionUtils.createAndAppend(this.roles, roles);
    }

    public void addRole(@NonNull Long roleIds) {
        this.roles = CollectionUtils.createAndAppend(this.roles, roleIds);
    }

    public void remove(@NonNull Long roleId) {
        CollectionUtils.nullSafeRemove(this.roles, roleId);
    }

    @Transient
    public boolean hasRole(Long roleId) {
        return CollectionUtils.nullSafeContains(this.roles, roleId);
    }

    public void enable() {
        this.active = true;
    }

    public void disable() {
        this.active = false;
    }
}

