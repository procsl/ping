package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.base.domain.user.Account;
import cn.procsl.ping.boot.domain.valid.UniqueField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户注册DTO")
public class RegisterDTO implements Serializable {

    @Schema(description = "用户账户", example = "program_chen@foxmail.com")
    @UniqueField(fieldName = "name", entity = Account.class, message = "用户账户已存在")
    @NotBlank @Size(min = 5, max = 50) String account;

    @Schema(description = "用户密码", example = "123456")
    @NotBlank @Size(min = 5, max = 20) String password;

}
