package cn.procsl.ping.boot.user.api;


import cn.procsl.ping.boot.user.UserService;
import cn.procsl.ping.boot.user.account.AccountEntity;
import cn.procsl.ping.boot.user.account.AccountService;
import cn.procsl.ping.boot.user.api.dto.RegisterUserDTO;
import cn.procsl.ping.boot.user.rbac.AccessControlService;
import cn.procsl.ping.boot.user.rbac.SubjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.constraints.NotNull;
import java.util.Set;

@RequiredArgsConstructor
public class UserApi {

    final UserService userService;

    final AccountService accountService;

    final AccessControlService accessControlService;

    final Set<String> defaultRegisterRoleNames;

    @Transactional
    public void register(@NotNull @RequestBody @Validated RegisterUserDTO userDTO) {
        Long userId = userService.register(userDTO);

        AccountEntity account = AccountEntity.builder().name(userDTO.getAccount()).password(userDTO.getPassword()).userId(userId).build();
        Long accountId = accountService.create(account);

        accessControlService.grant(SubjectEntity.builder().subjectId(accountId).roles(defaultRegisterRoleNames).build());
    }

}
