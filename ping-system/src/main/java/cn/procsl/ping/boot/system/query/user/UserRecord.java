package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.domain.user.Gender;
import cn.procsl.ping.boot.web.encrypt.SecurityId;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface UserRecord {

    @SecurityId
    @Schema(description = "用户ID")
    Long getId();

    @Schema(description = "用户昵称")
    String getName();

    @Schema(description = "用户性别")
    Gender getGender();

    @Schema(description = "用户备注")
    String getRemark();

    @Schema(description = "用户账户信息")
    UserAccountRecord getAccount();

}
