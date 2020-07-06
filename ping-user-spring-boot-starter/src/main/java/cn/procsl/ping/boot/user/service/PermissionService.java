package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

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
     * @param roleId     角色ID
     * @param resourceId 资源ID 资源视为外部数据 不会做合法性校验
     * @throws BusinessException 如果添加失败,则抛出此异常
     */
    public void grant(@NotNull Long roleId, @NotNull Long resourceId) throws BusinessException {
        Role role = roleService.load(roleId);

        resourceService.exists(resourceId);

        role.grantPermission(resourceId);

        roleJpaRepository.save(role);
    }

    /**
     * 删除权限
     *
     * @param roleId     指定的角色
     * @param resourceId 待删除的资源ID
     * @throws BusinessException 如果该资源不支持从该角色中删除, 则抛出异常
     */
    public void revoke(@NotNull Long roleId, @NotNull Long resourceId) throws BusinessException {
        Role role = roleService.load(roleId);

        role.revokePermission(resourceId);

        roleJpaRepository.save(role);
    }


    /**
     * 检测指定的角色是否具有指定的权限(资源)
     *
     * @param roleId     指定的角色ID
     * @param resourceId 指定的资源ID
     * @return 返回是否具有此权限
     */
    @Transactional(readOnly = true)
    public boolean has(@NotNull Long roleId, @NotNull Long resourceId) {
        Role role = roleService.load(roleId);
        return role.hasPermission(resourceId);
    }


    /**
     * 获取指定角色的所有权限
     *
     * @param roleId 指定的角色
     * @return 返回权限列表
     */
    @Transactional(readOnly = true)
    public Set<Long> getPermissions(@NotNull Long roleId) {

        Role role = roleService.load(roleId);
        if (role.getPermissions() == null) {
            return EMPTY_SET;
        }
        return Collections.unmodifiableSet(role.getPermissions());
    }

}
