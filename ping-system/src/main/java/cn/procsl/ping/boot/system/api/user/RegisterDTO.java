package cn.procsl.ping.boot.system.api.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO implements Serializable {

    @Schema(description = "用户昵称", example = "张三")
    @Size(max = 20) String nickName;

    @Schema(description = "用户账户", example = "zhangsan")
    @NotBlank @Size(min = 5, max = 50) String account;

    @Schema(description = "用户密码", example = "zhangsanpassword")
    @NotBlank @Size(min = 5, max = 20) String password;

}
