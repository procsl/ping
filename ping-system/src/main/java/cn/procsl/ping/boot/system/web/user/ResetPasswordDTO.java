package cn.procsl.ping.boot.system.web.user;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ResetPasswordDTO {

    @NotBlank
    @Size(min = 5, max = 20)
    String password;

}
