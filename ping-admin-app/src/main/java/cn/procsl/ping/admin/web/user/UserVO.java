package cn.procsl.ping.admin.web.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVO extends UserPropDTO {

    @Schema(description = "用户ID")
    Long id;

}
