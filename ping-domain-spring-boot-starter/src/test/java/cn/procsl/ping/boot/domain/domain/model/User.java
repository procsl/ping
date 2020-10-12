package cn.procsl.ping.boot.domain.domain.model;

import cn.procsl.ping.business.domain.DomainId;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

import static cn.procsl.ping.business.domain.DomainEntity.UUID_LENGTH;

/**
 * 用户对实体对象
 *
 * @author procsl
 * @date 2019/12/14
 */
@Entity(name = "$domain.user")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@Setter
@NoArgsConstructor
public class User implements DomainId<String> {

    @Id
    @Column(length = UUID_LENGTH)
    @GenericGenerator(name = "idGenerator", strategy = "uuid2")
    @GeneratedValue(generator = "idGenerator")
    protected String id;

    @Column(length = UUID_LENGTH, nullable = false)
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
