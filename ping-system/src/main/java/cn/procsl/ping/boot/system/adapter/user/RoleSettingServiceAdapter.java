package cn.procsl.ping.boot.system.adapter.user;

import cn.procsl.ping.boot.system.domain.user.RoleSettingService;
import cn.procsl.ping.boot.system.service.ConfigFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Indexed
@Slf4j
@Service
@RequiredArgsConstructor
class RoleSettingServiceAdapter implements RoleSettingService {

    public static final String DEFAULT_ROLES_CONFIG_KEY = "默认角色";
    final ConfigFacade configFacade;


    @Override
    public Collection<String> getDefaultRoles() {
        String config = configFacade.get(DEFAULT_ROLES_CONFIG_KEY);
        if (ObjectUtils.isEmpty(config)) {
            return Collections.emptyList();
        }
        return Arrays.asList(config.split(","));
    }

    @Override
    public void defaultRoleSetting(Collection<String> roles) {
        configFacade.put(DEFAULT_ROLES_CONFIG_KEY, String.join(",", roles));
    }


}
