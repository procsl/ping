package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.base.domain.user.AccountState;
import cn.procsl.ping.boot.domain.jpa.state.Stateful;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AccountStateDTO implements Stateful<AccountState> {

    @NotNull
    AccountState state;

}
