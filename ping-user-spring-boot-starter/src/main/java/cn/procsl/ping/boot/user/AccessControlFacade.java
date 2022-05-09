package cn.procsl.ping.boot.user;

import java.util.Set;

public interface AccessControlFacade {
    void grant(Long userId, Set<String> defaultRegisterRoleNames);
}
