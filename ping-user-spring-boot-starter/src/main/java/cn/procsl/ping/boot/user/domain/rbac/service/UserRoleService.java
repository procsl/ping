package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.rbac.repository.RoleRepository;
import cn.procsl.ping.boot.user.domain.rbac.repository.UserRoleRepository;

import javax.inject.Inject;

/**
 * @author procsl
 * @date 2020/04/19
 */
public class UserRoleService {

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    RoleRepository roleRepository;

}
