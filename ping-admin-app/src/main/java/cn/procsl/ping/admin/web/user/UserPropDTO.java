package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.base.domain.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPropDTO implements Serializable {

    @NotEmpty
    @Schema(description = "用户昵称")
    String name;

    Gender gender;

    @Schema(description = "用户备注")
    String remark;
}
