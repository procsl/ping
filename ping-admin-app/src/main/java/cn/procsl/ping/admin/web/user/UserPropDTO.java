package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.infra.domain.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class UserPropDTO implements Serializable {

    @NotEmpty
    @Schema(description = "用户昵称")
    String name;

    Gender gender;

    @Schema(description = "用户备注")
    String remark;
}
