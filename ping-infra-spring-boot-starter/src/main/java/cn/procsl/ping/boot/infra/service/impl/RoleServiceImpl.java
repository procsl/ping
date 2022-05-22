package cn.procsl.ping.boot.infra.service.impl;


import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.boot.domain.valid.UniqueValidator;
import cn.procsl.ping.boot.infra.domain.rbac.Role;
import cn.procsl.ping.boot.infra.service.RoleService;
import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;

/**
 * @name 访问控制模块
 * @description RBAC/MAC
 */
@Indexed
@Service
@RequiredArgsConstructor
@Validated
class RoleServiceImpl implements RoleService {

    final JpaRepository<Role, Long> roleRepository;

    final UniqueValidator uniqueValidator;

    /**
     * 创建角色
     *
     * @param name        角色名称, 必须唯一
     * @param permissions 权限列表
     * @return 创建成功返回ID
     * @throws BusinessException 如果创建失败
     */
    @Override
    @Transactional
    public Long create(@NotNull @UniqueField(entity = Role.class, fieldName = "name", message = "角色已存在") String name, @NotNull @Size(max = 100) Collection<@NotBlank @Size(max = 200) String> permissions) throws BusinessException {
        Role entity = new Role(name, permissions);
        return roleRepository.save(entity).getId();
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果删除失败
     */
    @Override
    @Transactional
    public void delete(@NotNull Long roleId) throws BusinessException {
        this.roleRepository.deleteById(roleId);
    }

    /**
     * 修改指定角色的权限
     *
     * @param id          role id
     * @param permissions 权限列表
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    @Override
    @Transactional
    public void changePermissions(@NotNull Long id, @NotNull @Size(max = 100) Collection<@NotBlank @Size(max = 200) String> permissions) throws BusinessException {
        Role role = this.roleRepository.getById(id);
        role.changePermissions(permissions);
        this.roleRepository.save(role);
    }

    /**
     * @param id   角色ID
     * @param name 角色名称
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    @Override
    @Transactional
    public void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws ConstraintViolationException {
        uniqueValidator.valid(Role.class, id, "name", name, "角色已存在");
        Role role = this.roleRepository.getById(id);
        role.setName(name);
        this.roleRepository.save(role);
    }


}
