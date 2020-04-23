package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.user.domain.common.GeneralEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

/**
 * 用户身份
 * @author procsl
 */
@Entity
@Table
@Data
@NoArgsConstructor
@Description(comment = "用户身份聚合根")
public class Identity extends GeneralEntity {

    private final static String roleColumnName = "role_id";

    @Id
    @Column(length = 20, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 32, nullable = false, updatable = false)
    @Description(comment = "用户ID,不可更新,直接和用户绑定, 用户聚合根")
    private String userId;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {"identity_id", roleColumnName}))
    @ElementCollection
    @Column(name = roleColumnName, nullable = false, updatable = false, length = 20)
    @Description(comment = "用户角色IDs")
    private Set<Long> roles;

}

