package cn.procsl.business.user.entity;

import cn.procsl.business.user.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户对实体对象
 *
 * @author procsl
 * @date 2019/12/14
 */
@Entity
@Table(uniqueConstraints = {
        // 以下列建索引
        @UniqueConstraint(columnNames = {"account"}),
        @UniqueConstraint(columnNames = {"phone"}),
        @UniqueConstraint(columnNames = {"email"}),
})
@Getter
@Setter
public class User extends EntityObject<String> {

    @Id
    @Column(length = 32)
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    protected String id;

    @Column(nullable = false, length = 20)
    protected String name;

    @Enumerated
    protected UserDTO.Gender gender;

    @Column(nullable = false, length = 20)
    private String account;

    @Column(length = 50)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 32)
    private String password;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createDate;

    @Version
    @Column(nullable = false)
    protected Long version;

}
