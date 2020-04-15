package cn.procsl.ping.boot.rbac.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static cn.procsl.ping.boot.rbac.domain.model.Role.ROLE_PERMISSION;

/**
 * @author procsl
 * @date 2020/04/08
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@NoArgsConstructor
public class Permission implements Serializable {

    @Id
    @Column(length = 20, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 权限名称
     */
    @Column(length = 20, nullable = false)
    private String name;

    @JoinTable(
            name = ROLE_PERMISSION,
            joinColumns = @JoinColumn(name = "permission_id", updatable = false, nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", updatable = false, nullable = false)
    )
    @ManyToMany(targetEntity = Role.class)
    private Set<Role> roles;

    @Builder(buildMethodName = "done", builderMethodName = "create")
    public Permission(String name, Set<Role> roles) {
        this.name = name;
        this.roles = roles;
    }
}
