package cn.procsl.ping.boot.system.api.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class AuthenticateDTO implements Serializable {

    @NotBlank
    @Schema(description = "用户账户")
    String account;

    @NotBlank
    @Schema(description = "用户密码")
    String password;

}
