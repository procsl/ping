package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.base.domain.user.AccountState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountVO extends AccountStateDTO {

    @Schema(description = "账户名称")
    String name;

    public AccountVO(String name, AccountState state) {
        this.name = name;
        this.state = state;
    }
}
