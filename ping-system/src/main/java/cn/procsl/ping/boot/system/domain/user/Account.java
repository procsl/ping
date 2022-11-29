package cn.procsl.ping.boot.system.domain.user;

import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import cn.procsl.ping.boot.common.jpa.state.Stateful;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * 用户账户
 */
@Getter
@Setter(value = AccessLevel.PACKAGE)
@Entity
@Table(name = "i_account")
@NoArgsConstructor
@AllArgsConstructor
@RepositoryCreator
public class Account extends AbstractPersistable<Long> implements Stateful<AccountState> {

    @Schema(description = "账户名称")
    @NotBlank
    String name;

    @Schema(description = "账户密码")
    @NotBlank
    String password;

    @Enumerated(value = EnumType.STRING)
    @Schema(description = "账户状态")
    @NotNull
    AccountState state;

    public static Account create(String name, String password) {
        return new Account(name, password, AccountState.enable);
    }

    public void enabled() {
        this.state = AccountState.enable;
    }

    public boolean isDisabled() {
        return AccountState.enable == state;
    }

    public void stateSetting(AccountState state) {
        this.setState(state);
    }


    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public boolean isEnable() {
        return state == AccountState.enable;
    }

    public void disabled() {
        this.state = AccountState.disable;
    }

    public void authenticate(String password) throws BusinessException {
        if (!this.isEnable()) {
            throw new AuthenticateException("账户已被禁用");
        }

        if (!this.checkPassword(password)) {
            throw new AuthenticateException("账户或密码错误");
        }
    }

    public void resetPassword(String password) {
        this.password = password;
    }
}
