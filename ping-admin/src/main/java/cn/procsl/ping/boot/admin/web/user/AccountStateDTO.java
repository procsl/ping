package cn.procsl.ping.boot.admin.web.user;

import cn.procsl.ping.boot.admin.domain.user.AccountState;
import cn.procsl.ping.boot.common.jpa.state.Stateful;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AccountStateDTO implements Stateful<AccountState> {

    @NotNull
    AccountState state;

}
