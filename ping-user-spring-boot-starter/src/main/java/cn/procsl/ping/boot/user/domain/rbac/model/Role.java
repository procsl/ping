package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.DefaultValue;
import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.data.annotation.IfEmptyAutoCreate;
import cn.procsl.ping.boot.user.domain.common.GeneralEntity;
import cn.procsl.ping.boot.user.domain.utils.CollectionsUtils;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static cn.procsl.ping.boot.user.domain.rbac.model.Permission.PERMISSION_ID_NAME;

/**
 * 用户角色
 *
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

    public final static String ROLE_ID_NAME = "role_id";

    public final static String REQUIRE_ID_NAME = "require_id";

    public final static String EXCLUDE_ID_NAME = "exclude_id";

    public final static Long EMPTY_ROLE_ID = -1L;

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(length = 50, nullable = false)
    @Description(comment = "角色名称")
    @IfEmptyAutoCreate(type = "name")
    protected String name;

    @Column(length = GENERAL_ENTITY_ID_LENGTH, nullable = false)
    @Description(comment = "继承的角色ID,不能是互斥的角色ID, 默认-1角色, 即空角色")
    @DefaultValue("-1")
    protected Long extendBy;

    @Column(length = 5, nullable = false)
    @Description(comment = "最大数量约束, 拥有此角色时当前角色的拥有者拥有的角色数量必须少于等于此值, 默认值为0, 表示不限制, 继承角色不计入此限制")
    @DefaultValue("0")
    private Integer max;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {ROLE_ID_NAME, REQUIRE_ID_NAME}))
    @ElementCollection
    @Column(name = REQUIRE_ID_NAME, updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "先决条件约束角色IDs, 当前角色的拥有者必须拥有此角色中的所有角色, 可以是继承的, 默认无约束")
    protected Set<Long> requires;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {ROLE_ID_NAME, EXCLUDE_ID_NAME}))
    @ElementCollection
    @Column(name = EXCLUDE_ID_NAME, updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "互斥的角色IDs, 不能是继承的ID")
    protected Set<Long> excludes;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {ROLE_ID_NAME, PERMISSION_ID_NAME}))
    @ElementCollection
    @Column(name = PERMISSION_ID_NAME, nullable = false, updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "权限IDs")
    protected Set<Long> permissions;

    @Builder(buildMethodName = "done", builderMethodName = "create")
    public Role(String name) {
        this.name = name;
    }

    public void addPermission(@NonNull Long... permissions) {
        this.permissions = CollectionsUtils.createAndAppend(this.permissions, permissions);
    }

}
