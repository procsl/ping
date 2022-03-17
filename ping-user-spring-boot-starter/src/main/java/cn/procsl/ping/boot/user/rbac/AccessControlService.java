package cn.procsl.ping.boot.user.rbac;


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
public class AccessControlService {

    final JpaRepository<SubjectEntity, Long> subjectRepository;

    final RoleRepository roleRepository;

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return
     * @throws BusinessException
     */
    public Long createRole(@NotNull Role role) throws BusinessException {
        if (this.roleRepository.exists(Example.of(RoleEntity.builder().name(role.getName()).build()))) {
            throw new BusinessException("权限名称已存在");
        }

        RoleEntity entity = new RoleEntity();
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
        RoleEntity role = this.roleRepository.getById(id);
        role.changePermissions(permissions);
        this.roleRepository.save(role);
    }

    /**
     * @param id   角色ID
     * @param name 角色名称
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    public void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws BusinessException {
        RoleEntity role = this.roleRepository.getById(id);
        role.setName(name);
        this.roleRepository.save(role);
    }

    /**
     * 分配角色
     *
     * @param subject 目标对象信息
     * @throws BusinessException 如果角色不存在
     */
    public void grant(@NotNull Subject<String> subject) throws BusinessException {
        Set<RoleEntity> roles = this.roleRepository.findRolesByNameIn(subject.getRoles());

        // 角色数量不同, 检测具体的角色并报错
        if (roles.size() < subject.getRoles().size()) {
            Set<String> set = roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());
            HashSet<String> names = new HashSet<>(subject.getRoles());
            names.removeAll(set);
            throw new BusinessException("该角色不存在[ {} ]", String.join(",", names));
        }

        Example<SubjectEntity> example = Example.of(SubjectEntity.builder().type(subject.getType()).subjectId(subject.getSubjectId()).build());
        Optional<SubjectEntity> option = this.subjectRepository.findOne(example);
        SubjectEntity entity = option.orElseGet(SubjectEntity::new);
        BeanUtils.copyProperties(subject, entity, "id", "roles");
        entity.setRoles(roles);
        this.subjectRepository.save(entity);
    }


}
