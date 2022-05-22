package cn.procsl.ping.boot.infra.domain.account;

import cn.procsl.ping.boot.domain.state.Stateful;
import cn.procsl.ping.exception.BusinessException;
import cn.procsl.ping.processor.annotation.RepositoryCreator;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Table(name = "i_account")
@NoArgsConstructor
@AllArgsConstructor
@RepositoryCreator
public class Account extends AbstractPersistable<Long> implements Stateful<AccountStatus> {

    @Schema(description = "用户姓名")
    String name;

    @Schema(description = "用户姓名")
    String password;

    @Enumerated(value = EnumType.STRING)
    @Schema(description = "账户状态")
    AccountStatus state;

    public static Account create(String name, String password) {
        return new Account(name, password, AccountStatus.enable);
    }

    public void enabled() {
        this.state = AccountStatus.enable;
    }

    public boolean isDisabled() {
        return AccountStatus.enable == state;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public boolean isEnable() {
        return state == AccountStatus.enable;
    }

    public void disabled() {
        this.state = AccountStatus.disable;
    }

    public void authenticate(String password) throws BusinessException {
        if (!this.isEnable()) {
            throw new BusinessException("账户已被禁用");
        }

        if (!this.checkPassword(password)) {
            throw new BusinessException("密码错误");
        }
    }

}
