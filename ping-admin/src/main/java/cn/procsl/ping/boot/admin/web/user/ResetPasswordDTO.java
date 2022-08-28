package cn.procsl.ping.boot.admin.web.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ResetPasswordDTO {

    @NotBlank
    @Size(min = 5, max = 20)
    String password;

}
