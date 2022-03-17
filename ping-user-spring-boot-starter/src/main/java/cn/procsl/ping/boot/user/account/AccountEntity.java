package cn.procsl.ping.boot.user.account;

import cn.procsl.ping.boot.domain.state.Stateful;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * 用户账户
 */
@Getter
@Setter
@Entity
@Table(name = "u_account")
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity extends AbstractPersistable<Long> implements Account, Stateful<AccountStatus> {

    Long userId;

    String name;

    String password;

    @Enumerated(value = EnumType.STRING)
    AccountStatus status;

    public AccountEntity(String name) {
        this.name = name;
    }

    public void enabled() {
        this.status = AccountStatus.enable;
    }

    public boolean isDisabled() {
        return AccountStatus.enable == status;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}
