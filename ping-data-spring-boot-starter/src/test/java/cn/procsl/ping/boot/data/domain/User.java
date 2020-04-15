package cn.procsl.ping.boot.data.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户对实体对象
 *
 * @author procsl
 * @date 2019/12/14
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@Setter
@NoArgsConstructor
public class User{

    @Id
    @Column(length = 32, updatable = false)
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    protected String id;

    @Column(length = 15, nullable = false)
    protected String name;

    @Version
    private Long version;

    @CreatedDate
    @Column(updatable = false)
    private Date createDate;

    @Builder
    public User(String id, String name, Long version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }

}
