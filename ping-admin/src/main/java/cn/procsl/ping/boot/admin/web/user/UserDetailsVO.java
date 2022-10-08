package cn.procsl.ping.boot.admin.web.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailsVO extends UserVO {

    @Schema(description = "用户账户信息")
    AccountVO account;

    @Schema(description = "用户部门信息")
    DepartmentDTO department;

}
