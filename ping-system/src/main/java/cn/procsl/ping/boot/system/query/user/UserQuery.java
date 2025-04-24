package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.Select;
import cn.procsl.ping.boot.system.domain.user.AccountState;
import cn.procsl.ping.boot.system.domain.user.Gender;
import cn.procsl.ping.boot.system.domain.user.User;
import lombok.Data;

@Data
@Projection(entity = User.class, alias = "u")
public class UserQuery {

    Long id;

    @Select
    String name;

    @Select(expression = "u.account.name")
    String account;

    @Select(expression = "u.account.state")
    AccountState state;

    @Select
    Gender gender;
}
