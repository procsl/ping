package cn.procsl.ping.boot.rbac;


import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.boot.domain.valid.UniqueValidation;
import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.criteria.*;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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

    final JpaSpecificationExecutor<Subject> subjectJpaSpecificationExecutor;

    final UniqueValidation uniqueValidation;

    /**
     * 创建角色
     *
     * @param name        角色名称, 必须唯一
     * @param permissions 权限列表
     * @return 创建成功返回ID
     * @throws BusinessException 如果创建失败
     */
    @Transactional
    public Long createRole(@NotNull @UniqueField(entity = Role.class, fieldName = "name", message = "角色已存在") String name, @NotNull @Size(max = 100) Collection<@NotBlank @Size(max = 200) String> permissions) throws BusinessException {
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
    public void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws ConstraintViolationException {
        uniqueValidation.valid(Role.class, id, "name", name, "角色已存在");
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
        List<Role> roles = this.specificationExecutor.findAll((root, query, cb) -> {
            CriteriaBuilder.In<String> in = cb.in(root.get("name"));
            for (String name : roleNames) {
                in.value(name);
            }
            return in;
        });

        // 角色数量不同, 检测具体的角色并报错
        if (roles.size() < roleNames.size()) {
            Set<String> set = roles.stream().map(Role::getName).collect(Collectors.toSet());
            HashSet<String> names = new HashSet<>(roleNames);
            names.removeAll(set);
            throw new BusinessException("该角色不存在[ {} ]", String.join(",", names));
        }

        Example<Subject> example = Example.of(Subject.builder().subject(subjectId).build());
        Optional<Subject> option = this.subjectRepository.findOne(example);
        Subject entity = option.orElseGet(Subject::new);
        entity.setSubject(subjectId);
        entity.addRoles(roles);
        this.subjectRepository.save(entity);
    }

    /**
     * 判断指定 subject 是否具有某种权限
     *
     * @param subject    目标subject
     * @param permission 权限
     * @return 如果存在该权限
     */
    public boolean hasPermission(@NotNull Long subject, @NotEmpty String permission) {
        long result = this.subjectJpaSpecificationExecutor.count(
                (root, query, cb) -> {
                    Join<Subject, Role> join = root.join("roles", JoinType.INNER);
                    SetJoin<Role, Permission> joinPermissions = join.joinSet("permissions", JoinType.INNER);

                    Predicate condition1 = cb.equal(root.get("subject"), subject);
                    Predicate condition2 = cb.equal(joinPermissions.get("name"), permission);
                    return cb.and(condition1, condition2);
                });
        return result > 0;
    }

    public boolean hasRole(Long subject, String role) {
        return false;
    }

}
