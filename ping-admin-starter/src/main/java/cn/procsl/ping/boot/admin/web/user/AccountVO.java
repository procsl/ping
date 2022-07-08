package cn.procsl.ping.boot.admin.web.user;

import cn.procsl.ping.boot.admin.domain.user.AccountState;
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
