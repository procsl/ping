package cn.procsl.ping.boot.infra.account;

import cn.procsl.ping.boot.domain.state.Stateful;
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
@Entity(name = "account")
@Table(name = "i_account")
@NoArgsConstructor
@AllArgsConstructor
@RepositoryCreator
class Account extends AbstractPersistable<Long> implements Stateful<AccountStatus> {

    @Schema(description = "用户姓名")
    String name;

    @Schema(description = "用户姓名")
    String password;

    @Enumerated(value = EnumType.STRING)
    @Schema(description = "账户状态")
    AccountStatus state;

    public Account(String name) {
        this.name = name;
    }

    public Account(String name, String password) {
        this.name = name;
        this.password = password;
        this.enabled();
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
}
