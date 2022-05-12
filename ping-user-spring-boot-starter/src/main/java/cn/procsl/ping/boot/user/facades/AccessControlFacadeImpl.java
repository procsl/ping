package cn.procsl.ping.boot.user.facades;

import cn.procsl.ping.boot.domain.utils.StringUtils;
import cn.procsl.ping.boot.rbac.RoleService;
import cn.procsl.ping.boot.system.ConfigDTO;
import cn.procsl.ping.boot.system.ConfigService;
import cn.procsl.ping.boot.user.AccessControlFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Indexed
@Service
@RequiredArgsConstructor
public class AccessControlFacadeImpl implements AccessControlFacade {

    final RoleService roleService;

    public static final String DEFAULT_ROLES_CONFIG_KEY = "默认角色";
    final ConfigService configService;

    @Override
    public void grant(Long userId, Collection<String> defaultRegisterRoleNames) {
        this.roleService.grant(userId, defaultRegisterRoleNames);
    }

    @Override
    public void grantDefaultRoles(Long userId) {
        this.grant(userId, this.getDefaultRoles());
    }


    @Override
    public Collection<String> getDefaultRoles() {
        String config = configService.getConfig(DEFAULT_ROLES_CONFIG_KEY);
        if (StringUtils.isEmpty(config)) {
            return Collections.emptyList();
        }
        return Arrays.asList(config.split(","));
    }

    @Override
    public void addDefaultRoles(Collection<String> roles) {
        ConfigDTO dto = new ConfigDTO(DEFAULT_ROLES_CONFIG_KEY, String.join(",", roles), "这是用户注册时默认授予的角色");
        configService.add(dto);
    }


}
