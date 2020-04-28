package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.user.domain.common.GeneralEntity;
import cn.procsl.ping.boot.user.domain.utils.CollectionsUtils;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

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
public class Identity extends GeneralEntity {

    private final static String roleColumnName = "role_id";

    @Id
    @Column(length = 20, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {"identity_id", roleColumnName}))
    @ElementCollection
    @Column(name = roleColumnName, nullable = false, updatable = false, length = 20)
    @Description(comment = "用户角色IDs")
    private Set<Long> roles;

    public void addRole(@NonNull Long roleIds) {
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

