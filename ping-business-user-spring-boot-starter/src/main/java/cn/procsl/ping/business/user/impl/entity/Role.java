package cn.procsl.ping.business.user.impl.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * 角色表
 *
 * @author procsl
 * @date 2020/03/21
 */
@Data
@Entity
@Table
public class Role {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToMany(targetEntity = Permission.class)
    private Set<Permission> permissions;

    @Version
    protected Long version;
}
