package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.domain.jpa.state.Stateful;
import cn.procsl.ping.boot.infra.domain.user.AccountState;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AccountStateDTO implements Stateful<AccountState> {

    @NotNull
    AccountState state;

}
