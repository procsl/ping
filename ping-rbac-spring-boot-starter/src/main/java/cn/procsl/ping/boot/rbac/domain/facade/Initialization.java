package cn.procsl.ping.boot.rbac.domain.facade;

import cn.procsl.ping.boot.rbac.config.RbacProperties;
import cn.procsl.ping.boot.rbac.domain.model.*;
import cn.procsl.ping.boot.rbac.domain.repository.PermissionRepository;
import cn.procsl.ping.boot.rbac.domain.repository.RoleRepository;
import cn.procsl.ping.boot.rbac.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;

/**
 * 初始化服务, 用于初始化一些系统默认的数据
 *
 * @author procsl
 * @date 2020/04/08
 */
@RequiredArgsConstructor
@Slf4j
public class Initialization {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PermissionRepository permissionRepository;

    final RbacProperties properties;

    /**
     * 初始化超级用户
     */
    protected void initSuperUser(String root, String superUserName) {
        // 如果系统中不存在root用户 则初始化一个
        User superUser = User.create().name(root).done();

        // 添加默认角色
        this.roleRepository
                .findOne(QRole.role.name.eq(superUserName))
                .ifPresent(role -> {
                    superUser.addRole(role);
                    userRepository.save(superUser);
                });
    }

    /**
     * 初始化默认角色
     */
    protected void initDefaultRole(String defaultRoleName, String[] permissionNames) {
        boolean exists = roleRepository.exists(QRole.role.name.eq(defaultRoleName));
        if (!exists) {
            Role defaultRole = Role.create().name(defaultRoleName).done();
            roleRepository.save(defaultRole);

            // 添加默认的权限列表
            Iterable<Permission> tmp = this.permissionRepository.findAll(QPermission.permission.name.in(permissionNames));
            for (Permission permission : tmp) {
                defaultRole.addPermission(permission);
            }
            roleRepository.save(defaultRole);
        }
    }

    /**
     * 初始化默认权限列表
     */
    protected void initDefaultPermissions(String[] permissionNames) {
        LinkedList list = new LinkedList();
        for (String permissionName : permissionNames) {
            if (permissionRepository.exists(QPermission.permission.name.eq(permissionName))) {
                break;
            }
            Permission perm = Permission.create().name(permissionName).done();
            list.add(perm);
        }

        if (list.isEmpty()) {
            return;
        }
        permissionRepository.saveAll(list);
    }

    /**
     * 执行初始化的动作
     */
    @Transactional(rollbackFor = Exception.class)
    public void action() {
        String root = "root";
        boolean exists = userRepository.exists(QUser.user.name.eq("root"));
        if (exists) {
            log.info("系统默认用户[{}]已存在, 取消默认用户数据初始化", root);
            return;
        }

        String[] permissionNames = {"login", "manager", "menu"};
        this.initDefaultPermissions(permissionNames);

        String defaultRoleName = "超级管理员";
        this.initDefaultRole(defaultRoleName, permissionNames);

        this.initSuperUser(root, defaultRoleName);
    }
}
