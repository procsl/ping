package cn.procsl.ping.boot.user.domain.user.model;

import cn.procsl.ping.business.domain.DomainId;
import cn.procsl.ping.business.exception.BusinessException;
import cn.procsl.ping.processor.repository.annotation.RepositoryCreator;
import com.google.common.hash.Hashing;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.FetchType.LAZY;

/**
 * 用户账户
 */
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
@Setter(AccessLevel.PROTECTED)
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@Entity(name = "$user:account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@RepositoryCreator
@EntityListeners({AuditingEntityListener.class})
public class Account implements DomainId<Long> {

    public static final int ACCOUNT_MIN = 6;

    public static final int ACCOUNT_MAX = 20;

    public static final int PASSWORD_LEN = 32;


    @Id
    @Access(AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_sequences")
    Long id;

    @Column(length = ACCOUNT_MAX, nullable = false)
    String name;

    @Column(length = PASSWORD_LEN, nullable = false)
    String password;

    @Column(length = PASSWORD_LEN, nullable = false)
    String salt;

    @OneToOne(fetch = LAZY)
    @JoinColumn(referencedColumnName = "id")
    User user;

    @Version
    @Column(nullable = false)
    @ColumnDefault("0")
    Long version;

    public Account(String name, String password, User user) {
        this.name = name;
        this.password = password;
        this.user = user;
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        // TODO
        this.salt = Hashing.md5().hashBytes(new Date().toString().getBytes()).toString();
    }

    /**
     * 登录
     *
     * @param password 密码
     */
    public void validatePassword(String password) {
        if (!this.password.equals(password)) {
            throw new BusinessException("密码错误");
        }
    }
}
