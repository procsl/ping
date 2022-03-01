package cn.procsl.ping.boot.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Schema(description = "用户注册对象")
public class RegisterUserDTO implements Serializable {

    @NotNull
    @NotBlank
    String userName;

    @NotNull
    String gender;

    @NotNull
    @NotBlank
    String accountName;

    @NotNull
    @NotBlank
    String password;

}
