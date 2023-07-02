package cn.procsl.ping.boot.system.api.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO implements Serializable {

    @NotBlank @Size(min = 5, max = 50) String account;

    @NotBlank @Size(min = 5, max = 20) String password;

}
