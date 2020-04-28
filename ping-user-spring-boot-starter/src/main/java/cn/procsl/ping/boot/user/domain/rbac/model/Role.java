package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.user.domain.common.GeneralEntity;
import cn.procsl.ping.boot.user.domain.utils.CollectionsUtils;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * 用户角色
 * @author procsl
 * @date 2020/04/06
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@NoArgsConstructor
@Setter
@Description(comment = "用户角色作为单独的聚合根")
public class Role extends GeneralEntity implements Serializable {

    protected final static String PERMISSION = "permission_id";

    @Id
    @Column(length = 20, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    @Description(comment = "角色名称")
    private String name;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", PERMISSION}))
    @ElementCollection
    @Column(name = PERMISSION, nullable = false, updatable = false, length = 20)
    @Description(comment = "权限IDs")
    private Set<Long> permissions;

    @Builder(buildMethodName = "done", builderMethodName = "create")
    public Role(String name) {
        this.name = name;
    }

    public void addPermission(@NonNull Long... permissions) {
        this.permissions = CollectionsUtils.createAndAppend(this.permissions, permissions);
    }
}
