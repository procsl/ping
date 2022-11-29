package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.common.jpa.state.Stateful;
import cn.procsl.ping.boot.system.domain.user.AccountState;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountStateDTO implements Stateful<AccountState> {

    @NotNull
    AccountState state;

}
