package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.system.domain.user.AccountState;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountVO extends AccountStateDTO {

    String name;

    public AccountVO(String name, AccountState state) {
        this.name = name;
        this.state = state;
    }
}
