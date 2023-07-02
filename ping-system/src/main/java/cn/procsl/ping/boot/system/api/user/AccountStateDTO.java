package cn.procsl.ping.boot.system.api.user;

import cn.procsl.ping.boot.jpa.domain.state.Stateful;
import cn.procsl.ping.boot.system.domain.user.AccountState;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class AccountStateDTO implements Stateful<AccountState> {

    @NotNull
    AccountState state;

}
