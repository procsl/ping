package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.QRole;
import cn.procsl.ping.boot.user.domain.rbac.entity.QSession;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import cn.procsl.ping.boot.user.domain.rbac.entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static cn.procsl.ping.boot.user.domain.rbac.entity.Role.NAME_LENGTH;

/**
 * 角色 服务接口
 * 实现基本的rbac1 基础功能, 能够实现角色的继承
 *
 * @author procsl
 * @date 2020/06/24
 */
@Named
@Singleton
@RequiredArgsConstructor
@Validated
@Transactional(rollbackFor = Exception.class)
public class RoleService {


    @Inject
    final JpaRepository<Role, Long> roleJpaRepository;

    @Inject
    final QuerydslPredicateExecutor<Role> roleQueryDslPredicateExecutor;

    @Inject
    final QuerydslPredicateExecutor<Session> identityQueryDslPredicateExecutor;

    /**
     * 创建角色
     *
     * @param name          角色名称
     * @param inheritRoleId 继承的角色
     * @return 返回角色ID
     * @throws BusinessException 如果角儿创建失败,例如角色已经存在 抛出此异常
     */
    public Long create(@NotBlank @Size(min = 1, max = NAME_LENGTH) String name, Long inheritRoleId) throws BusinessException {
        existsName(name);

        checkInheritable(inheritRoleId);

        Role newRole = Role.creator().name(name).inheritBy(inheritRoleId).done();
        return roleJpaRepository.save(newRole).getId();
    }


    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果角色被绑定, 则不可删除
     */
    public void delete(@NotNull Long roleId) throws BusinessException {
        boolean exists = identityQueryDslPredicateExecutor.exists(QSession.session.roles.contains(roleId));
        if (exists) {
            throw new BusinessException("该角色已被使用");
        }
        roleJpaRepository.deleteById(roleId);
    }


    /**
     * 角色重命名
     *
     * @param roleId 指定的角色
     * @param name   角色新名称
     * @throws BusinessException 如果该名称被占用, 则抛出异常
     */
    public void rename(@NotNull Long roleId, @NotBlank @Size(min = 1, max = NAME_LENGTH) String name) throws BusinessException {
        Role role = load(roleId);

        checkNameable(role, name);

        role.rename(name);

        this.roleJpaRepository.save(role);
    }

    /**
     * 修改继承角色
     *
     * @param roleId        操作的角色
     * @param inheritRoleId 继承的角色
     * @throws BusinessException 业务异常
     */
    public void changeInherit(@NotNull Long roleId, @NotNull Long inheritRoleId) throws BusinessException {
        checkInheritable(inheritRoleId);

        Role role = load(roleId);

        role.changeInherit(inheritRoleId);

        roleJpaRepository.save(role);
    }


    /**
     * 加载角色
     *
     * @param roleId 角色Id
     * @return 加载成功后的角色
     * @throws BusinessException 如果角色不存在则抛出异常
     */
    @Transactional(readOnly = true)
    public Role load(@NotNull Long roleId) throws BusinessException {
        return roleJpaRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
    }


    /**
     * 检验角色是否可以继承
     *
     * @param inheritRoleId 角色ID
     * @throws BusinessException 如果不可继承则抛出异常
     */
    @Transactional(readOnly = true)
    public void checkInheritable(Long inheritRoleId) throws BusinessException {
        boolean existId = inheritRoleId == null ||
                Role.EMPTY_ROLE_ID >= inheritRoleId ||
                roleJpaRepository.existsById(inheritRoleId);
        if (!existId) {
            throw new BusinessException("指定继承的角色不存在");
        }
    }


    /**
     * 检查是否可以命名名称
     *
     * @param roleId 角色id
     * @param name   指定的名称
     * @throws BusinessException 如果不能修改的名称则抛出异常
     */
    @Transactional(readOnly = true)
    public void checkNameable(@NotNull Long roleId, @NotBlank @Size(min = 1, max = NAME_LENGTH) String name) throws BusinessException {
        Role role = load(roleId);
        checkNameable(role, name);
    }

    private void checkNameable(Role role, String name) throws BusinessException {
        if (role.getName().equals(name)) {
            return;
        }
        existsName(name);
    }

    private void existsName(String name) throws BusinessException {
        boolean exists = this.roleQueryDslPredicateExecutor.exists(QRole.role.name.eq(name));
        if (exists) {
            throw new BusinessException("该角色名称已被占用");
        }
    }

}
