package cn.procsl.ping.boot.user.facades;

import cn.procsl.ping.boot.rbac.RoleService;
import cn.procsl.ping.boot.user.AccessControlFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import java.util.Set;

@Indexed
@Service
@RequiredArgsConstructor
public class AccessControlFacadeImpl implements AccessControlFacade {

    final RoleService roleService;

    @Override
    public void grant(Long userId, Set<String> roles) {
        roleService.grant(userId, roles);
    }
}
