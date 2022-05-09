package cn.procsl.ping.boot.rbac;


import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @name 访问控制模块
 * @description RBAC/MAC
 */
@Indexed
@Service
@RequiredArgsConstructor
public class RoleService {

    final JpaRepository<Subject, Long> subjectRepository;

    final RoleRepository roleRepository;

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return
     * @throws BusinessException
     */
    public Long createRole(@NotNull Role role) throws BusinessException {
        if (this.roleRepository.exists(Example.of(new Role(role.getName())))) {
            throw new BusinessException("权限名称已存在");
        }

        Role entity = new Role();
        BeanUtils.copyProperties(role, entity, "id");
        return roleRepository.save(entity).getId();
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果删除失败
     */
    public void deleteRole(@NotNull Long roleId) throws BusinessException {
        this.roleRepository.deleteById(roleId);
    }

    /**
     * 修改指定角色的权限
     *
     * @param id          role id
     * @param permissions 权限列表
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    public void changeRolePermissions(@NotNull Long id, @NotNull @Size(max = 100) Collection<@Max(100) String> permissions) throws BusinessException {
        Role role = this.roleRepository.getById(id);
        role.changePermissions(permissions);
        this.roleRepository.save(role);
    }

    /**
     * @param id   角色ID
     * @param name 角色名称
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    public void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws BusinessException {
        Role role = this.roleRepository.getById(id);
        role.setName(name);
        this.roleRepository.save(role);
    }

    /**
     * 分配角色
     *
     * @param subjectId 目标对象信息
     * @param roleNames 角色名称
     * @throws BusinessException 如果角色不存在
     */
    public void grant(@NotNull Long subjectId, @NotNull Collection<String> roleNames) throws BusinessException {
        Set<Role> roles = this.roleRepository.findRolesByNameIn(roleNames);

        // 角色数量不同, 检测具体的角色并报错
        if (roles.size() < roleNames.size()) {
            Set<String> set = roles.stream().map(Role::getName).collect(Collectors.toSet());
            HashSet<String> names = new HashSet<>(roleNames);
            names.removeAll(set);
            throw new BusinessException("该角色不存在[ {} ]", String.join(",", names));
        }

        Example<Subject> example = Example.of(Subject.builder().subjectId(subjectId).build());
        Optional<Subject> option = this.subjectRepository.findOne(example);
        Subject entity = option.orElseGet(Subject::new);
        entity.setSubjectId(subjectId);
        entity.setRoles(roles);
        this.subjectRepository.save(entity);
    }


}
