package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.common.service.PasswordEncoderService;
import cn.procsl.ping.boot.system.domain.user.User;
import cn.procsl.ping.boot.web.annotation.Changed;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "users")
@Slf4j
public class AccountController {


    final JpaRepository<User, Long> userRepository;

    final PasswordEncoderService passwordEncoderService;

    @Changed(path = "/v1/system/users/{id}/account", summary = "修改用户账户状态")
    public void accountStatusSetting(@PathVariable Long id, @Validated @RequestBody AccountStateDTO state) {
        userRepository
                .getReferenceById(id)
                .getAccount()
                .stateSetting(state.getState());
    }

    @Changed(path = "/v1/system/users/{id}/password", summary = "重置用户密码")
    public void resetPassword(@PathVariable Long id, @Validated @RequestBody ResetPasswordDTO passwordDTO) {
        userRepository
                .getReferenceById(id)
                .getAccount()
                .resetPassword(passwordEncoderService.encode(passwordDTO.getPassword()));
    }

}
