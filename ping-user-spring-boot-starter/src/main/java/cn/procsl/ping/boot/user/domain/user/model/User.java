package cn.procsl.ping.boot.user.domain.user.model;

import cn.procsl.ping.boot.domain.annotation.RepositoryCreator;
import cn.procsl.ping.boot.domain.business.common.model.CommonEntity;
import cn.procsl.ping.boot.domain.business.state.model.BooleanStateful;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import cn.procsl.ping.business.exception.BusinessException;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Data
@Setter(AccessLevel.PROTECTED)
@ToString(exclude = {"account", "profile", "createBy", "createAt"})
@EqualsAndHashCode(exclude = {"account", "profile", "createBy", "createAt"})
@Table
@Entity(name = "$user:user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@EntityListeners({DomainEventListener.class, AuditingEntityListener.class})
@RepositoryCreator
@DynamicUpdate
public class User implements CommonEntity<String>, BooleanStateful<String> {

    final static int NAME_LEN = 20;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = UUID_LENGTH)
    @Access(AccessType.PROPERTY)
    String id;

    @Column(length = NAME_LEN, nullable = false)
    String nickname;

    @Setter(AccessLevel.PUBLIC)
    @Column(nullable = false)
    Boolean state;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id")
    Account account;

    @Embedded
    Profile profile;

    @OneToOne(fetch = FetchType.LAZY)
    User createBy;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    @OrderBy
    Long createAt;

    @Version
    @ColumnDefault("0")
    @Column(nullable = false, insertable = false)
    Long version;

    public User(String name, String account, String password, String createBy) {
        this.nickname = name;
        this.profile = new Profile(name);
        this.account = new Account(account, password, this);
        this.enable();

        if (createBy == null) {
            this.createBy = this;
        } else {
            this.createBy = new User();
            this.createBy.setId(createBy);
        }
    }

    /**
     * 修改名称
     *
     * @param name 新名称
     */
    public void changeName(String name) {
        this.nickname = name;
    }


    /**
     * 重置密码
     *
     * @param password 新密码
     */
    public void resetPassword(String password) {
        this.account.setPassword(password);
    }


    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param password    新密码
     * @throws BusinessException 如果旧密码错误,则抛出此异常
     */
    public void changePassword(String oldPassword, String password) throws BusinessException {
        if (!this.state) {
            throw new BusinessException("当前账户被禁用");
        }

        if (oldPassword.equals(password)) {
            return;
        }

        // 校验密码
        this.account.validatePassword(oldPassword);
        this.account.setPassword(password);
    }

    @Override
    public boolean isEnable() {
        return state;
    }


}
