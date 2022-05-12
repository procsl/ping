package cn.procsl.ping.boot.rbac;


import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.boot.domain.valid.UniqueService;
import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @name 访问控制模块
 * @description RBAC/MAC
 */
@Indexed
@Service
@RequiredArgsConstructor
@Validated
public class RoleService {

    final JpaRepository<Subject, Long> subjectRepository;

    final JpaRepository<Role, Long> roleRepository;

    final JpaSpecificationExecutor<Role> specificationExecutor;

    final UniqueService uniqueService;

    /**
     * 创建角色
     *
     * @param name        角色名称, 必须唯一
     * @param permissions 权限列表
     * @return 创建成功返回ID
     * @throws BusinessException 如果创建失败
     */
    @Transactional
    public Long createRole(@NotNull @UniqueField(entity = Role.class, fieldName = "name", message = "角色已存在") String name,
                           @NotNull @Size(max = 100) Collection<@NotBlank @Size(max = 200) String> permissions) throws BusinessException {
        Role entity = new Role(name, permissions);
        return roleRepository.save(entity).getId();
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果删除失败
     */
    @Transactional
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
    @Transactional
    public void changeRolePermissions(@NotNull Long id, @NotNull @Size(max = 100) Collection<@NotBlank @Size(max = 200) String> permissions) throws BusinessException {
        Role role = this.roleRepository.getById(id);
        role.changePermissions(permissions);
        this.roleRepository.save(role);
    }

    /**
     * @param id   角色ID
     * @param name 角色名称
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    @Transactional
    public void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws BusinessException {
        uniqueService.valid(Role.class, id, "name", name, "角色已存在");
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
    @Transactional
    public void grant(@NotNull Long subjectId, @NotNull Collection<String> roleNames) throws BusinessException {
        List<Role> roles = this.specificationExecutor.findAll((root, query, cb) -> cb.in(root.get("name").in(roleNames)));

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
        entity.addRoles(roles);
        this.subjectRepository.save(entity);
    }


}
