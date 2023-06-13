package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.common.service.PasswordEncoderService;
import cn.procsl.ping.boot.system.domain.user.User;
import cn.procsl.ping.boot.web.annotation.Changed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "users")
@Slf4j
public class AccountController {


    final JpaRepository<User, Long> userRepository;

    final PasswordEncoderService passwordEncoderService;

    @Operation(summary = "修改用户账户状态")
    @PatchMapping(path = "/v1/system/users/{id}/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackOn = Exception.class)
    public void accountStatusSetting(@PathVariable Long id, @Validated @RequestBody AccountStateDTO state) {
        userRepository
                .getReferenceById(id)
                .getAccount()
                .stateSetting(state.getState());
    }

    @Operation(summary = "重置用户密码")
    @PatchMapping(path = "/v1/system/users/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackOn = Exception.class)
    public void resetPassword(@PathVariable Long id, @Validated @RequestBody ResetPasswordDTO passwordDTO) {
        userRepository
                .getReferenceById(id)
                .getAccount()
                .resetPassword(passwordEncoderService.encode(passwordDTO.getPassword()));
    }

}
