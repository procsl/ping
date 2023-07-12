package cn.procsl.ping.boot.system.api.user;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVO extends UserPropDTO {

    @Schema(description = "用户ID")
    @SecurityId
    Long id;

}
