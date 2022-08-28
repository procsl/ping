package cn.procsl.ping.boot.admin.web.user;

import cn.procsl.ping.boot.admin.domain.user.User;
import cn.procsl.ping.boot.common.service.PasswordEncoderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "users")
public class AccountController {

    final JpaRepository<User, Long> userRepository;

    final PasswordEncoderService passwordEncoderService;


    @Transactional
    @PatchMapping("/v1/users/{id}/account")
    @Operation(summary = "修改指定用户的账户状态")
    public void accountStatusSetting(@PathVariable Long id, @Validated @RequestBody AccountStateDTO state) {
        userRepository
                .getReferenceById(id)
                .getAccount().
                stateSetting(state.getState());
    }

    @Transactional
    @PatchMapping("/v1/users/{id}/password")
    @Operation(summary = "重置指定用户密码")
    public void resetPassword(@PathVariable Long id, @Validated @RequestBody ResetPasswordDTO passwordDTO) {
        userRepository
                .getReferenceById(id)
                .getAccount()
                .resetPassword(passwordEncoderService.encode(passwordDTO.getPassword()));
    }

}
