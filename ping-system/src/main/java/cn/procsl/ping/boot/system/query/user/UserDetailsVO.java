package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.web.user.AccountVO;
import cn.procsl.ping.boot.system.web.user.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailsVO extends UserVO {

    @Schema(description = "用户账户信息")
    AccountVO account;

}
