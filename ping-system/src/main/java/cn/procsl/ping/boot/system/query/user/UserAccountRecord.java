package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.domain.user.AccountState;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface UserAccountRecord {

    @Schema(description = "账户名称")
    String getName();


    @Schema(description = "账户状态")
    AccountState getState();

}
