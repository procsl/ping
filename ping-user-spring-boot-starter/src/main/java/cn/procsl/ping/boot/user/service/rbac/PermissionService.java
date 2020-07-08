package cn.procsl.ping.boot.user.service.rbac;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.command.rbac.PermissionCommand;
import cn.procsl.ping.boot.user.domain.rbac.entity.Permission;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

import static cn.procsl.ping.boot.user.utils.CollectionUtils.isEmpty;
import static java.util.Collections.EMPTY_SET;

/**
 * @author procsl
 * @date 2020/07/05
 */
@Named
@Singleton
@RequiredArgsConstructor
@Validated
@Transactional(rollbackFor = Exception.class)
public class PermissionService {

    @Inject
    final JpaRepository<Role, Long> roleJpaRepository;

    @Inject
    final RoleService roleService;

    @Inject
    final ResourceService resourceService;

    /**
     * 为指定的角色添加权限
     *
     * @param command 权限修改命令
     * @throws BusinessException 如果添加失败,则抛出此异常
     */
    public void grant(@NonNull @Valid PermissionCommand command) throws BusinessException {
        Role role = roleService.load(command.getRoleId());

        role.grantPermission(command.getPermission());

        roleJpaRepository.save(role);
    }

    /**
     * 删除权限
     *
     * @param command 权限修改命令
     * @throws BusinessException 如果该资源不支持从该角色中删除, 则抛出异常
     */
    public void revoke(@NonNull @Valid PermissionCommand command) throws BusinessException {
        Role role = roleService.load(command.getRoleId());

        role.revokePermission(command.getPermission());

        roleJpaRepository.save(role);
    }


    /**
     * 检测指定的角色是否具有指定的权限(资源)
     *
     * @param command 权限修改命令
     * @return 返回是否具有此权限
     */
    @Transactional(readOnly = true)
    public boolean has(@NonNull @Valid PermissionCommand command) {
        Role role = roleService.load(command.getRoleId());
        return role.hasPermission(command.getPermission());
    }


    /**
     * 获取指定角色的所有权限
     *
     * @param roleId 指定的角色
     * @return 返回权限列表
     */
    @Transactional(readOnly = true)
    public Set<Permission> getPermissions(@NotNull Long roleId) {

        Role role = roleService.load(roleId);
        if (isEmpty(role.getPermissions())) {
            return EMPTY_SET;
        }
        return Collections.unmodifiableSet(role.getPermissions());
    }

}
