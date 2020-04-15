package cn.procsl.ping.boot.rbac.domain.model;

import cn.procsl.ping.boot.rbac.domain.utils.CollectionsUtils;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static cn.procsl.ping.boot.rbac.domain.model.User.USER_ROLE;

/**
 * @author procsl
 * @date 2020/04/06
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Setter
public class Role implements Serializable {
    protected final static String ROLE_PERMISSION = "role_permission";

    @Id
    @Column(length = 20, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色名称
     */
    @Column(length = 20, nullable = false)
    private String name;

    @ManyToMany(targetEntity = User.class)
    @JoinTable(
            name = USER_ROLE,
            joinColumns = @JoinColumn(name = "role_id", updatable = false, nullable = false),
            inverseJoinColumns = @JoinColumn(name = "user_id", updatable = false, nullable = false)
    )
    private Set<User> users;

    /**
     * 权限列表
     */
    @ManyToMany(targetEntity = Permission.class)
    @JoinTable(
            name = ROLE_PERMISSION,
            joinColumns = @JoinColumn(name = "role_id", updatable = false, nullable = false),
            inverseJoinColumns = @JoinColumn(name = "permission_id", updatable = false, nullable = false)
    )
    private Set<Permission> permissions;

    /**
     * 版本号
     */
    @Version
    @ColumnDefault("0")
    @Column(nullable = false, insertable = false)
    private Long version;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(updatable = false, nullable = false)
    @OrderBy
    private Long createDate;

    @Builder(buildMethodName = "done", builderMethodName = "create")
    public Role(String name, Set<User> users, Set<Permission> permissions) {
        this.name = name;
        this.users = users;
        this.permissions = permissions;
    }

    public void addPermission(@NonNull Permission... permissions) {
        this.permissions = CollectionsUtils.createAndAppend(this.permissions, permissions);
    }
}
