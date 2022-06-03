package cn.procsl.ping.admin.web.account;

import cn.procsl.ping.boot.infra.domain.user.Account;
import cn.procsl.ping.boot.infra.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "account", description = "账户模块接口")
public class AccountController {

    final JpaRepository<Account, Long> jpaRepository;

    final JpaRepository<User, Long> userRepository;


    @PatchMapping("/v1/users/{id}/account")
    @Operation(summary = "获取用户信息")
    @Transactional
    public void accountStatusSetting(@PathVariable Long id, @RequestBody StateDTO state) {
        userRepository.getById(id)
                .getAccount().
                stateSetting(state.getState());
    }

}
