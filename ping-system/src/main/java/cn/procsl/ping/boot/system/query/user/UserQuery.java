package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.jpa.support.query.From;
import cn.procsl.ping.boot.system.domain.user.AccountState;
import cn.procsl.ping.boot.system.domain.user.Gender;
import cn.procsl.ping.boot.system.domain.user.User;
import lombok.Data;

@Data
@From(entity = User.class, alias = "u")
public class UserQuery {

    String name;

    String account;

    AccountState state;

    Gender gender;
}
