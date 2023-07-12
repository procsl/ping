package cn.procsl.ping.boot.system.api.user;

import cn.procsl.ping.boot.system.domain.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@NoArgsConstructor
public class UserDetailVO extends UserVO {

    @Schema(description = "用户账户信息")
    AccountVO account;

    public UserDetailVO(Long id, String name, Gender gender, String remark, AccountVO account) {
        this.account = account;
        this.setId(id);
        this.setName(name);
        this.setGender(gender);
        this.setRemark(remark);
    }
}
