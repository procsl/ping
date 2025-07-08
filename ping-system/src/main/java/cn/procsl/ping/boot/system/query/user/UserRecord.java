package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.domain.user.Gender;
import cn.procsl.ping.boot.web.annotation.SecurityId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRecord {

    @SecurityId(scope = "user")
    @Schema(description = "用户ID")
    Long id;

    @Schema(description = "用户昵称")
    String name;

    @Schema(description = "用户性别")
    Gender gender;

    @Schema(description = "用户备注")
    String remark;

    @Schema(description = "用户账户信息")
    UserAccountRecord account;

}
