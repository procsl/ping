package cn.procsl.ping.boot.system.web.setting;

import cn.procsl.ping.boot.system.domain.user.RoleSettingService;
import cn.procsl.ping.boot.web.Patch;
import cn.procsl.ping.boot.web.Query;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "setting", description = "系统设置模块")
public class RoleSettingController {

    final RoleSettingService roleSettingService;

    @Patch(path = "/v1/system/setting/default-roles", summary = "设置默认授权角色")
    public void defaultRoleSetting(@RequestBody Collection<String> roles) {
        roleSettingService.defaultRoleSetting(roles);
    }


    @Query(path = "/v1/system/setting/default-roles", summary = "获取默认角色设置")
    public Collection<String> getDefaultRoles() {
        return roleSettingService.getDefaultRoles();
    }

}
