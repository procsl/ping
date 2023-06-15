package cn.procsl.ping.boot.system.query;

import cn.procsl.ping.boot.jpa.FormatPage;
import cn.procsl.ping.boot.system.domain.user.AccountState;
import cn.procsl.ping.boot.system.domain.user.Gender;
import cn.procsl.ping.boot.web.annotation.MarkPageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "用户管理模块接口")
public class UserQueryController {

    @MarkPageable
    @Operation(summary = "获取用户列表")
    @GetMapping("/v1/system/users")
    public FormatPage<UserDetailsVO> findUsers(Pageable pageable,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false, name = "account.name") String account,
                                               @RequestParam(required = false, name = "account.state") AccountState state,
                                               @RequestParam(required = false) Gender gender) {

        return null;
    }

}
