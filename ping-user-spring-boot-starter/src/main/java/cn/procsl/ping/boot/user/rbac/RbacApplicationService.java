package cn.procsl.ping.boot.user.rbac;


import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.POST;
import java.util.Collection;
import java.util.Optional;

/**
 * @name 角色管理模块
 * @description RBAC模块之一， 用于管理角色
 */
@Named("rbacApplicationService")
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
    public void deleteRole(@NotNull Long roleId) throws RbacException {
        this.roleJpaRepository.deleteById(roleId);
    }

    /**
     * 修改指定角色的权限
     *
     * @param id          role id
     * @param permissions 权限列表
     * @throws RbacException 如果修改失败，则抛出异常
     */
    public void changeRolePermissions(@NotNull Long id, @NotNull @Size(max = 100) Collection<@Max(100) String> permissions) throws RbacException {
        Role role = this.roleJpaRepository.getById(id);
        this.verifyPermissionService.verify(permissions);
        role.changePermissions(permissions);
        this.roleJpaRepository.save(role);
    }

    /**
     * @param id   角色ID
     * @param name 角色名称
     * @throws RbacException 如果修改失败，则抛出异常
     */
    public void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws RbacException {
        Role role = this.roleJpaRepository.getById(id);
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
    public void changeRole(@NotNull Long id, @Size(max = 20) String name, @Size(max = 100) Collection<@Size(max = 100) String> permissions) throws RbacException {
        if (name != null) {
            this.changeRoleName(id, name);
        }
        if (permissions != null) {
            this.changeRolePermissions(id, permissions);
        }
    }

    /**
     * 获取角色
     *
     * @param id 角色ID
     * @return 返回角色信息
     */
    public Optional<Role> getById(Long id) {
        return this.roleJpaRepository.findById(id);
    }

    /**
     * 分页查询角色接口
     *
     * @param page  第几页
     * @param order 排序方式
     * @param size  每页大小
     * @return 返回分页的角色对象
     */
    public Page<Role> query(int page, int size, Sort.Direction order) {
        return this.roleJpaRepository.findAll(PageRequest.of(page, size, order, "name"));
    }


}
