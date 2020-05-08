package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.user.domain.common.GeneralEntity;
import cn.procsl.ping.boot.user.domain.utils.CollectionsUtils;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static cn.procsl.ping.boot.user.domain.common.GeneralEntity.GENERAL_ENTITY_ID_LENGTH;
import static cn.procsl.ping.boot.user.domain.rbac.model.Role.ROLE_ID_NAME;

/**
 * 表示外部实体 机构 用户等需要角色权限管理的实体
 *
 * @author procsl
 */
@Entity
@Table
@NoArgsConstructor
@Description(comment = "用户身份聚合根")
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class Identity implements Serializable {

    public final static String IDENTITY_ID_NAME = "identity_id";

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {IDENTITY_ID_NAME, ROLE_ID_NAME}))
    @ElementCollection
    @Column(name = ROLE_ID_NAME, nullable = false, updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "用户角色IDs")
    private Set<Long> roles;

    void addRole(@NonNull Long roleIds) {
        this.roles = CollectionsUtils.createAndAppend(this.roles, roleIds);
    }

    public void remove(@NonNull Long roleId) {
        if (this.roles == null) {
            return;
        }
        this.roles.remove(roleId);
    }

    @Transient
    public boolean hasRole(Long roleId) {
        if (this.roles == null) {
            return false;
        }
        return this.roles.contains(roleId);
    }
}

