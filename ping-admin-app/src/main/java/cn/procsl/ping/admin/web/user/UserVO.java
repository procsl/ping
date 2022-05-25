package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.infra.domain.user.Gender;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserVO implements Serializable {

    Long id;

    String name;

    Gender gender;

    String remark;

}
