package cn.procsl.ping.app.admin.dto;

import cn.procsl.ping.boot.user.domain.user.model.Account;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    @Size(max = 50)
    String realName;

    @NotEmpty
    @Size(min = Account.ACCOUNT_MIN, max = Account.ACCOUNT_MAX)
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "账号只能使用(字母，数字，下划线)")
    String account;

    @NotEmpty @Size(min = 8, max = 15)
    String password;

}
