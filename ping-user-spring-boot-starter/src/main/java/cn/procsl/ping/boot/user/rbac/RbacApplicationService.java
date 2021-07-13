package cn.procsl.ping.boot.user.rbac;


import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import java.util.Collection;

/**
 * @name 角色管理模块
 * @description RBAC模块之一， 用于管理角色
 */
@Named("rbacApplicationService")
@Path("roles")
@NoArgsConstructor
public class RbacApplicationService {

    JpaRepository<Role, Long> roleJpaRepository;

    VerifyPermissionService verifyPermissionService;

    @Inject
    public RbacApplicationService(JpaRepository<Role, Long> roleJpaRepository, VerifyPermissionService verifyPermissionServiceProvider) {
        this.roleJpaRepository = roleJpaRepository;
        this.verifyPermissionService = verifyPermissionServiceProvider;
    }

    /**
     * 创建角色
     *
     * @param name        角色名称
     * @param permissions 权限
     */
    @POST
    @Transactional(rollbackOn = Exception.class)
    public Long createRole(@NotBlank @Size(max = 20) String name, @NotNull @Size(max = 100) Collection<@Size(max = 100) String> permissions) throws RbacException {
        this.verifyPermissionService.verify(permissions);
        Role role = roleJpaRepository.save(new Role(name, permissions));
        return role.getId();
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws RbacException 如果删除失败
     */
    @DELETE
    @Path("{role_id}")
    @Transactional(rollbackOn = Exception.class)
    public void deleteRole(@PathParam(value = "role_id") @NotNull Long roleId) throws RbacException {
        this.roleJpaRepository.deleteById(roleId);
    }

    /**
     * 修改指定角色的权限
     *
     * @param id          role id
     * @param permissions 权限列表
     * @throws RbacException 如果修改失败，则抛出异常
     */
    @Transactional(rollbackOn = Exception.class)
    public void changeRolePermissions(@NotNull Long id, @NotNull @Size(max = 100) Collection<@Max(100) String> permissions) throws RbacException {
        Role role = this.roleJpaRepository.getOne(id);
        this.verifyPermissionService.verify(permissions);
        role.changePermissions(permissions);
        this.roleJpaRepository.save(role);
    }

    /**
     * @param id   角色ID
     * @param name 角色名称
     * @throws RbacException 如果修改失败，则抛出异常
     */
    @Transactional(rollbackOn = Exception.class)
    public void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws RbacException {
        Role role = this.roleJpaRepository.getOne(id);
        role.setName(name);
        this.roleJpaRepository.save(role);
    }

    /**
     * 修改角色信息
     *
     * @param id          角色ID
     * @param name        角色名称
     * @param permissions 权限列表
     * @throws RbacException 如果修改失败，则抛出异常
     */
    @PATCH
    @Transactional(rollbackOn = Exception.class)
    public void changeRole(@NotNull Long id, @Size(max = 20) String name, @Size(max = 100) Collection<@Size(max = 100) String> permissions) throws RbacException {
        if (name != null) {
            this.changeRoleName(id, name);
        }
        if (permissions != null) {
            this.changeRolePermissions(id, permissions);
        }
    }

}
