package cn.procsl.ping.boot.system.web.setting;

import cn.procsl.ping.boot.system.domain.user.RoleSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Setting", description = "系统设置模块")
public class RoleSettingController {

    final RoleSettingService roleSettingService;

    @Operation(summary = "设置默认授权角色")
    @PutMapping(path = "/v1/system/setting/default-roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void defaultRoleSetting(@RequestBody Collection<String> roles) {
        roleSettingService.defaultRoleSetting(roles);
    }


    @Operation(summary = "获取默认角色设置")
    @GetMapping(path = "/v1/system/setting/default-roles")
    @Transactional(readOnly = true)
    public Collection<String> getDefaultRoles() {
        return roleSettingService.getDefaultRoles();
    }

}
