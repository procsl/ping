package cn.procsl.ping.boot.user.domain.rbac.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.data.business.entity.GeneralEntity;
import cn.procsl.ping.boot.user.utils.CollectionsUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

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

    public final static String PERMISSION_ID_NAME = "resource_id";

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
    protected Long inheritBy;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {ROLE_ID_NAME, PERMISSION_ID_NAME}))
    @ElementCollection
    @Column(name = PERMISSION_ID_NAME, updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "权限IDs 实际为资源ID")
    protected Set<Long> permissions;

    public Role(String name) {
        this.name = name;
    }

    /**
     * 设置继承的角色, 可以设置为null
     *
     * @param parent 父节点ID
     */
    public void changeInherit(@Nullable Long parent) {
        if (parent == null || parent <= EMPTY_ROLE_ID) {
            this.inheritBy = EMPTY_ROLE_ID;
            return;
        }
        this.inheritBy = parent;
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
                Long inheritBy,
                Set<Long> permissions) {
        this.rename(name);
        this.changeInherit(inheritBy);
        this.permissions = permissions;
    }

    public void grantPermission(Long permissions) {
        this.permissions = CollectionsUtils.createAndAppend(this.permissions, permissions);
    }

    public void revokePermission(Long resourceId) {
        CollectionsUtils.nullSafeRemove(this.permissions, resourceId);
    }

    public boolean hasPermission(Long resourceId) {
        return CollectionsUtils.nullSafeContains(this.permissions, resourceId);
    }
}
