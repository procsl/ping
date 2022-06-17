package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.base.domain.user.Account;
import cn.procsl.ping.boot.domain.valid.UniqueField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO implements Serializable {

    @UniqueField(fieldName = "name", entity = Account.class, message = "用户账户已存在")
    @NotBlank @Size(min = 5, max = 50) String account;

    @NotBlank @Size(min = 5, max = 20) String password;

}
