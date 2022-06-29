package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.base.domain.user.AccountState;
import cn.procsl.ping.boot.domain.jpa.state.Stateful;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AccountStateDTO implements Stateful<AccountState> {

    @Schema(description = "账户状态")
    @NotNull
    AccountState state;

}
