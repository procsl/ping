package cn.procsl.ping.boot.user.domain.rbac.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.data.business.entity.GeneralEntity;
import cn.procsl.ping.boot.user.utils.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static cn.procsl.ping.boot.user.domain.rbac.entity.Role.ROLE_ID_NAME;
import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PRIVATE;

/**
 * 角色关系
 * 用于处理/约束角色
 *
 * @author procsl
 * @date 2020/07/05
 */
@Entity
@Table
@Getter
@NoArgsConstructor
@Setter(PRIVATE)
@Description(comment = "角色关系约束")
@CreateRepository
public class Relation extends GeneralEntity {

    public static final String RELATION_ID_NAME = "relation_id";

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Column(length = 15, nullable = false)
    private RelationType type;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(length = 1000)
    @Description(comment = "用于解释角色关系")
    private String script;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {ROLE_ID_NAME, RELATION_ID_NAME}))
    @ElementCollection
    @Column(name = ROLE_ID_NAME, updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "受到关系约束的角色ID")
    private Set<Long> roles;

    public void changeRoles(@NonNull Collection<Long> roles) {
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("roles 不可为空");
        }
        this.roles = new HashSet<>(roles);
    }

    public void changeScript(String script) {
        if (script != null && script.length() > 1000) {
            throw new IllegalArgumentException("script不可超过1000");
        }
        this.script = script;
    }

    public void changeName(@NonNull String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("名称不可为空");
        }

        if (name.length() > 20) {
            throw new IllegalArgumentException("名称不可超过20");
        }
        this.name = name;
    }

    public void changeType(@NonNull RelationType type) {
        this.type = type;
    }


    Relation(RelationType type, String name, String script, Collection<Long> roles) {
        this.changeName(name);
        this.changeType(type);
        this.changeScript(script);
        this.changeRoles(roles);
    }


    public static Relation create(String name, RelationType type, String script, Collection<Long> roles) {
        return new Relation(type, name, script, roles);
    }
}
