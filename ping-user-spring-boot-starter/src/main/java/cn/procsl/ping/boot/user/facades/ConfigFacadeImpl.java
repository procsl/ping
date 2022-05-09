package cn.procsl.ping.boot.user.facades;

import cn.procsl.ping.boot.system.Config;
import cn.procsl.ping.boot.system.ConfigService;
import cn.procsl.ping.boot.user.ConfigFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Indexed
@Service
@RequiredArgsConstructor
public class ConfigFacadeImpl implements ConfigFacade {

    final ConfigService configService;

    @Override
    public Set<String> getDefaultRoles() {
        Optional<Config> option = configService.getConfigByKey("默认角色");
        if (option.isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(option.get().getText().split(",")).collect(Collectors.toUnmodifiableSet());
    }
}
