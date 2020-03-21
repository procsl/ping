package cn.procsl.ping.business.user.impl.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

/**
 * 用户对实体对象
 *
 * @author procsl
 * @date 2019/12/14
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
public class User {

    @Id
    @Column(length = 32)
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    protected String id;

    @Column(length = 15, nullable = false)
    protected String name;

    /**
     * 该用户的角色列表
     */
    @ManyToMany(targetEntity = Role.class)
    protected Set<Role> roles;

}
