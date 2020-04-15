package cn.procsl.ping.boot.rbac.domain.model;

import cn.procsl.ping.boot.rbac.domain.utils.CollectionsUtils;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author procsl
 * @date 2020/04/06
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@ToString
@Setter
public class User implements Serializable {

    public static final String USER_ROLE = "user_role";

    @Id
    @Column(length = 32, updatable = false)
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    private String id;

    /**
     * 用户名称
     */
    @Column(length = 20, nullable = false)
    private String name;

    /**
     * 用户角色
     */
    @ManyToMany(targetEntity = Role.class)
    @JoinTable(
            name = USER_ROLE,
            joinColumns = @JoinColumn(updatable = false, name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(updatable = false, name = "role_id", nullable = false)
    )
    private Set<Role> roles;

    /**
     * 版本 乐观锁
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
    public User(String name, Set<Role> roles) {
        this.name = name;
        this.roles = roles;
    }

    public void addRole(@NonNull Role... roles) {
        this.roles = CollectionsUtils.createAndAppend(this.roles, roles);
    }

    public void addRole(@NonNull List<Role> roles) {
        this.roles = CollectionsUtils.createAndAppend(this.roles, roles);
    }

    public boolean removeRole(Role role) {
        if (this.roles == null) {
            return false;
        }
        return this.roles.remove(role);
    }
}
