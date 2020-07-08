package cn.procsl.ping.boot.user.service.rbac;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.command.rbac.ChangeRoleInheritCommand;
import cn.procsl.ping.boot.user.command.rbac.CreateRoleCommand;
import cn.procsl.ping.boot.user.command.rbac.RenameRoleCommand;
import cn.procsl.ping.boot.user.domain.rbac.entity.QRole;
import cn.procsl.ping.boot.user.domain.rbac.entity.QSession;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import cn.procsl.ping.boot.user.domain.rbac.entity.Session;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    final QuerydslPredicateExecutor<Session> sessionQueryDslPredicateExecutor;

    /**
     * 创建角色
     *
     * @param command 创建角色表征
     * @return 返回角色ID
     * @throws BusinessException 如果角儿创建失败,例如角色已经存在 抛出此异常
     */
    public Long create(@NonNull @Valid CreateRoleCommand command) throws BusinessException {
        existsName(command.getName());

        Role parentRole = null;
        if (command.getInheritRoleId() != null) {
            parentRole = load(command.getInheritRoleId());
        }

        Role newRole = Role.create(command.getName(), parentRole, command.getPermissions());

        return roleJpaRepository.save(newRole).getId();
    }


    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @throws BusinessException 如果角色被绑定, 则不可删除
     */
    public void delete(@NotNull Long roleId) throws BusinessException {
        boolean exists = sessionQueryDslPredicateExecutor.exists(QSession.session.roles.contains(roleId));
        if (exists) {
            throw new BusinessException("该角色已被使用");
        }
        roleJpaRepository.deleteById(roleId);
    }


    /**
     * 角色重命名
     *
     * @param command 重命名角色表征
     * @throws BusinessException 如果该名称被占用, 则抛出异常
     */
    public void rename(@NonNull @Valid RenameRoleCommand command) throws BusinessException {
        Role role = load(command.getRoleId());

        checkNameable(role, command.getName());

        role.changeName(command.getName());

        this.roleJpaRepository.save(role);
    }

    /**
     * 修改继承角色
     *
     * @param command 修改角色继承表征
     * @throws BusinessException 业务异常
     */
    public void changeInherit(@NonNull @Valid ChangeRoleInheritCommand command) throws BusinessException {

        Role role = load(command.getRoleId());
        Role parent = null;
        if (command.getInheritRoleId() != null) {
            parent = load(command.getInheritRoleId());
        }

        role.changeInherit(parent);

        roleJpaRepository.save(role);
    }


    /**
     * 检查是否可以命名名称
     *
     * @param command 重命名角色表征
     * @throws BusinessException 如果不能修改的名称则抛出异常
     */
    @Transactional(readOnly = true)
    public void checkNameable(@NonNull @Valid RenameRoleCommand command) throws BusinessException {
        Role role = load(command.getRoleId());
        checkNameable(role, command.getName());
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

    Role load(@NotNull Long roleId) throws BusinessException {
        return roleJpaRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
    }

}
