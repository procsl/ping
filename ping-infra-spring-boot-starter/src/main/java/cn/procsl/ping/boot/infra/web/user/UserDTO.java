package cn.procsl.ping.boot.infra.web.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {

    @NotBlank @Size(min = 5, max = 50) String account;

    @NotBlank @Size(min = 5, max = 20) String password;

}
