package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.domain.state.Stateful;
import cn.procsl.ping.boot.infra.domain.user.AccountState;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class StateDTO implements Stateful<AccountState> {

    @NotNull
    AccountState state;

}
