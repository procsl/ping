package cn.procsl.ping.boot.infra.user.facades;

import cn.procsl.ping.boot.infra.conf.ConfigService;
import cn.procsl.ping.boot.infra.rbac.AccessControlService;
import cn.procsl.ping.boot.infra.rbac.RoleService;
import cn.procsl.ping.boot.infra.user.AccessControlFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Indexed
@Service
@RequiredArgsConstructor
public class AccessControlFacadeImpl implements AccessControlFacade {

    public static final String DEFAULT_ROLES_CONFIG_KEY = "默认角色";
    final RoleService roleService;
    final AccessControlService accessControlService;
    final ConfigService configService;

    @Override
    public void grant(Long userId, Collection<String> roleNames) {
        this.accessControlService.grant(userId, roleNames);
    }

    @Override
    public void grantDefaultRoles(Long userId) {
        this.grant(userId, this.getDefaultRoles());
    }


    @Override
    public Collection<String> getDefaultRoles() {
        String config = configService.getConfig(DEFAULT_ROLES_CONFIG_KEY);
        if (ObjectUtils.isEmpty(config)) {
            return Collections.emptyList();
        }
        return Arrays.asList(config.split(","));
    }

    @Override
    public void defaultRoleSetting(Collection<String> roles) {
        configService.add(DEFAULT_ROLES_CONFIG_KEY, String.join(",", roles), "这是用户注册时默认授予的角色");
    }


}
