package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.rbac.repository.RoleRepository;
import cn.procsl.ping.boot.user.domain.rbac.repository.UserRoleRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author procsl
 * @date 2020/04/19
 */
@Named
@Singleton
public class UserRoleService {

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    RoleRepository roleRepository;

}
