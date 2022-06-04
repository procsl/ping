package cn.procsl.ping.admin.web.setting;

import cn.procsl.ping.boot.infra.domain.user.RoleSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "setting", description = "系统设置模块")
public class SettingController {

    final RoleSettingService roleSettingService;

    @Transactional
    @PatchMapping("/v1/setting/default-roles")
    @Operation(summary = "设置默认授权角色")
    public void defaultRoleSetting(@RequestBody Collection<String> roles) {
        roleSettingService.defaultRoleSetting(roles);
    }


    @Transactional(readOnly = true)
    @GetMapping("/v1/setting/default-roles")
    @Operation(summary = "获取默认角色设置")
    public Collection<String> getDefaultRoles() {
        return roleSettingService.getDefaultRoles();
    }

}
