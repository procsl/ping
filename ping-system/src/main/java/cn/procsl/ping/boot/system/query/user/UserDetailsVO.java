package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.domain.user.Gender;
import cn.procsl.ping.boot.system.api.user.AccountVO;
import cn.procsl.ping.boot.system.api.user.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailsVO extends UserVO {

    @Schema(description = "用户账户信息")
    AccountVO account;

    public UserDetailsVO(Long id, String name, Gender gender, String remark, AccountVO account) {
        this.account = account;
        this.setId(id);
        this.setName(name);
        this.setGender(gender);
        this.setRemark(remark);
    }
}
