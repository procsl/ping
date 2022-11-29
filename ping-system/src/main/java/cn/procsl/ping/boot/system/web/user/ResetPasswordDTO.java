package cn.procsl.ping.boot.system.web.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {

    @NotBlank
    @Size(min = 5, max = 20)
    String password;

}
