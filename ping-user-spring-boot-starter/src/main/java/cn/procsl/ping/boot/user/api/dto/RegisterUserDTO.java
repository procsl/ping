package cn.procsl.ping.boot.user.api.dto;

import cn.procsl.ping.boot.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Schema(name = "用户注册DTO")
public class RegisterUserDTO implements User {

    @NotNull @NotBlank @Size(min = 5, max = 20)
    @Schema(defaultValue = "用户注册姓名")
    String name;

    @NotNull @NotBlank @Size(min = 5, max = 20)
    @Schema(defaultValue = "用户账户")
    String account;

    @NotNull @NotBlank @Size(min = 5, max = 20)
    @Schema(defaultValue = "用户密码")
    String password;

    @NotNull
    @Schema(defaultValue = "用户性别")
    Gender gender;

    @Size(max = 500)
    @Schema(defaultValue = "用户信息备注")
    String remark;

}
