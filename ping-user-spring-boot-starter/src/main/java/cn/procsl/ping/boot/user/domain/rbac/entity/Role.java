package cn.procsl.ping.boot.user.domain.rbac.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.DefaultValue;
import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.data.business.entity.GeneralEntity;
import cn.procsl.ping.boot.data.business.utils.BusinessException;
import cn.procsl.ping.boot.user.domain.utils.CollectionsUtils;
import lombok.*;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static cn.procsl.ping.boot.user.domain.rbac.entity.Permission.PERMISSION_ID_NAME;
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
@NoArgsConstructor
@Setter(PRIVATE)
@Description(comment = "用户角色作为单独的聚合根")
@CreateRepository
public class Role extends GeneralEntity implements Serializable {

    public final static String ROLE_ID_NAME = "role_id";

    public final static String REQUIRE_ID_NAME = "require_id";

    public final static String EXCLUDE_ID_NAME = "exclude_id";

    public final static Long EMPTY_ROLE_ID = -1L;

    public final static int NAME_LENGTH = 50;


    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(length = NAME_LENGTH, nullable = false)
    @Description(comment = "角色名称")
    protected String name;

    @Column(length = GENERAL_ENTITY_ID_LENGTH, nullable = false)
    @Description(comment = "继承的角色ID,当前角色拥有继承角色的所有权限, 与互斥的角色冲突。 默认-1角色, 即空角色")
    @DefaultValue(forFiled = "cn.procsl.ping.boot.user.domain.rbac.model.Role.EMPTY_ROLE_ID")
    protected Long extendBy;

    @Column(length = 5, nullable = false)
    @Description(comment = "最大数量约束, 拥有此角色时当前角色的拥有者拥有的角色数量必须少于等于此值, 默认值为0, 表示不限制, 继承角色不计入此限制")
    @DefaultValue("0")
    protected Integer max;

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

    public void removePermission(@NonNull Long... permissions) {
        CollectionsUtils.nullSafeRemove(this.permissions, permissions);
    }

    /**
     * 设置继承的角色, 可以设置为null
     *
     * @param parent 父节点ID
     */
    public void doExtendBy(@Nullable Long parent) {
        // TODO 获取注解的默认值 空角色
        if (parent == null || EMPTY_ROLE_ID.equals(parent)) {
            this.extendBy = EMPTY_ROLE_ID;
        }

        // 继承的角色不能存在于互斥角色中
        do {
            if (this.excludes == null) {
                break;
            }
            if (this.excludes.contains(parent)) {
                throw new BusinessException("扩展角色{}与互斥角色冲突", parent);
            }
        } while (false);
        this.extendBy = parent;
    }

    /**
     * 重命名角色名称
     *
     * @param name 新的角色名称
     */
    public void rename(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("角色名称不可为空");
        }

        if (name.length() > NAME_LENGTH) {
            throw new IllegalArgumentException("错误的角色名称");
        }
        this.name = name;
    }

    @Builder(buildMethodName = "done", builderMethodName = "creator")
    public Role(String name,
                Long extendBy,
                Integer max,
                Set<Long> requires,
                Set<Long> excludes,
                Set<Long> permissions) {
        this.rename(name);
        this.doExtendBy(extendBy);
        this.max = max;
        this.requires = requires;
        this.excludes = excludes;
        this.permissions = permissions;
    }
}
