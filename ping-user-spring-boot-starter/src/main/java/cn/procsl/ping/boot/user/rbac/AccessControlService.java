package cn.procsl.ping.boot.user.rbac;


import cn.procsl.ping.business.exception.BusinessException;
import lombok.RequiredArgsConstructor;
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

    final JpaRepository<Subject, Long> subjectRepository;

    final RoleRepository repository;

    /**
     * 创建角色
     *
     * @param name        角色名称
     * @param permissions 权限
     */
    public Long createRole(@NotBlank @Size(max = 20) String name, @NotNull @Size(max = 100) Collection<@Size(max = 100) String> permissions) throws BusinessException {
        if (this.repository.exists(Example.of(new Role(name)))) {
            throw new BusinessException(401, "U04", "权限已存在");
        }

        Role role = repository.save(new Role(name, permissions));
        return role.getId();
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果删除失败
     */
    public void deleteRole(@NotNull Long roleId) throws BusinessException {
        this.repository.deleteById(roleId);
    }

    /**
     * 修改指定角色的权限
     *
     * @param id          role id
     * @param permissions 权限列表
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    public void changeRolePermissions(@NotNull Long id, @NotNull @Size(max = 100) Collection<@Max(100) String> permissions) throws BusinessException {
        Role role = this.repository.getById(id);
        role.changePermissions(permissions);
        this.repository.save(role);
    }

    /**
     * @param id   角色ID
     * @param name 角色名称
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    public void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws BusinessException {
        Role role = this.repository.getById(id);
        role.setName(name);
        this.repository.save(role);
    }

    /**
     * 修改角色信息
     *
     * @param id          角色ID
     * @param name        角色名称
     * @param permissions 权限列表
     * @throws BusinessException 如果修改失败，则抛出异常
     */
    public void changeRole(@NotNull Long id, @Size(max = 20) String name, @Size(max = 100) Collection<@Size(max = 100) String> permissions) throws BusinessException {
        if (name != null) {
            this.changeRoleName(id, name);
        }
        if (permissions != null) {
            this.changeRolePermissions(id, permissions);
        }
    }


    /**
     * 分配角色
     *
     * @param userId    用户ID
     * @param roleNames 角色名称
     * @throws BusinessException 如果角色不存在
     */
    public void grant(@NotNull Long userId, @NotNull Collection<String> roleNames) throws BusinessException {
        Set<Role> roles = this.repository.findRolesByNameIn(roleNames);

        // 角色数量不同, 检测具体的角色并报错
        if (roles.size() < roleNames.size()) {
            Set<String> set = roles.stream().map(Role::getName).collect(Collectors.toSet());
            HashSet<String> names = new HashSet<>(roleNames);
            names.removeAll(set);
            throw new BusinessException(401, "U003", "该角色不存在[ " + String.join(",", names) + " ]");
        }

        Subject subject = new Subject();
        subject.setSubjectId(userId);
        subject.setRoles(roles);
        subject.setType("user");
        this.subjectRepository.save(subject);
    }

    /**
     * 撤销角色
     *
     * @param userId    指定的用户ID
     * @param roleNames 角色名称
     * @throws BusinessException 如果撤销失败
     */
    public void revoke(@NotNull Long userId, @NotNull Collection<String> roleNames) throws BusinessException {

        Optional<Subject> subject = this.subjectRepository.findOne(Example.of(new Subject(userId, "user", null)));

        subject.ifPresent(item -> {
            Set<Role> roles = this.repository.findRolesByNameIn(roleNames);
            item.getRoles().removeAll(roles);
            this.subjectRepository.save(item);
        });

        subject.orElseThrow(() -> new BusinessException(401, "U004", "授权对象不存在"));
    }

}
