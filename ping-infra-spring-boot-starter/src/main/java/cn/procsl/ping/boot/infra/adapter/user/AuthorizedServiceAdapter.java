package cn.procsl.ping.boot.infra.adapter.user;

import cn.procsl.ping.boot.infra.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.infra.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.infra.domain.user.AuthorizedService;
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
class AuthorizedServiceAdapter implements AuthorizedService {

    public static final String DEFAULT_ROLES_CONFIG_KEY = "默认角色";
    final AccessControlService accessControlService;
    final ConfigOptionService configService;

    @Override
    public void grant(Long userId, Collection<String> roleNames) {
        this.accessControlService.grant(userId, roleNames);
    }


    @Override
    public Collection<String> getDefaultRoles() {
        String config = configService.get(DEFAULT_ROLES_CONFIG_KEY);
        if (ObjectUtils.isEmpty(config)) {
            return Collections.emptyList();
        }
        return Arrays.asList(config.split(","));
    }

    @Override
    public void defaultRoleSetting(Collection<String> roles) {
        configService.put(DEFAULT_ROLES_CONFIG_KEY, String.join(",", roles));
    }


}
