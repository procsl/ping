package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.domain.business.state.repository.BooleanStatefulRepository;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.user.domain.common.service.AbstractBooleanStatefulService;
import cn.procsl.ping.boot.user.domain.common.service.AbstractService;
import cn.procsl.ping.boot.user.domain.rbac.model.Permission;
import cn.procsl.ping.boot.user.domain.rbac.model.QRole;
import cn.procsl.ping.boot.user.domain.rbac.model.Role;
import cn.procsl.ping.boot.user.domain.rbac.model.Target;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;

import static cn.procsl.ping.boot.user.domain.rbac.model.Role.ROLE_NAME_LEN;

/**
 * 角色相关的业务,应该做拦截做相关的事件处理
 */
@Slf4j
@Validated
@Transactional(rollbackFor = Exception.class)
public class RoleService extends AbstractService<Long, Role> implements AbstractBooleanStatefulService<Role, Long> {

    public static final QRole R = QRole.role;
    @Getter
    final BooleanStatefulRepository<Role, Long> booleanStatefulRepository;

    @Inject
    PermissionService permissionService;

    @Inject
    public RoleService(QuerydslPredicateExecutor<Role> querydslRepository,
                       PermissionService permissionService,
                       JpaRepository<Role, Long> jpaRepository,
                       BooleanStatefulRepository<Role, Long> booleanStatefulRepository
    ) {
        super(jpaRepository, querydslRepository);
        this.permissionService = permissionService;
        this.booleanStatefulRepository = booleanStatefulRepository;
    }

    /**
     * 创建角色
     *
     * @param name 角色名称
     * @return 角色创建成功之后返回角色ID
     * @throws IllegalArgumentException 如果父角色,或者权限不存在则抛出此异常
     */
    @Transactional
    public Long create(@NotBlank @Size(min = 1, max = ROLE_NAME_LEN) String name, Collection<? extends Target> targets) throws EntityNotFoundException {
        Collection<Permission> permissions = permissionService.getByTargets(targets);
        Role role = jpaRepository.save(new Role(name, permissions));
        return role.getId();
    }

    /**
     * 创建角色,通过指定父角色和权限ID
     *
     * @param name          角色名称
     * @param permissionIds 权限IDs
     * @return 创建成功返回角色ID
     * @throws EntityNotFoundException 如果权限IDs,或父角色ID未找到
     */
    @Transactional
    public Long createByIds(@NotBlank @Size(min = 1, max = ROLE_NAME_LEN) String name, Collection<Long> permissionIds) throws EntityNotFoundException {
        Collection<Permission> perms = this.permissionService.findByIds(permissionIds);
        Role role = jpaRepository.save(new Role(name, perms));
        return role.getId();
    }


    /**
     * 修改角色名称
     *
     * @param roleId 指定的角色ID
     * @param name   新的角色名称
     * @throws EntityNotFoundException 如果指定的角色不存在则抛出此异常
     */
    public void changName(@NotNull Long roleId, @NotBlank @Size(min = 1, max = ROLE_NAME_LEN) String name) throws EntityNotFoundException {
        BooleanExpression condition = R.id.eq(roleId).and(R.name.ne(name));
        this.querydslRepository
            .findOne(condition)
            .ifPresent((role) -> {
                role.setName(name);
                jpaRepository.save(role);
            });
    }


    /**
     * 修改角色指定的权限
     *
     * @param roleId  指定的角色ID
     * @param targets 权限Path
     * @throws IllegalArgumentException 如果指定的实体未找到
     */
    public void changePermission(@NotNull Long roleId, Collection<? extends Target> targets) throws IllegalArgumentException {
        Role role = getOne(roleId);
        Collection<Permission> perm = this.permissionService.getByTargets(targets);
        role.changePermissions(perm);
        this.jpaRepository.save(role);
    }

    /**
     * 通过权限ID修改权限
     *
     * @param roleId  指定的角色ID
     * @param permIds 权限ID
     * @throws IllegalArgumentException 如果参数不合法
     */
    public void changePermissionById(@NotNull Long roleId, Collection<Long> permIds) throws IllegalArgumentException {
        Role role = getOne(roleId);
        Collection<Permission> perms = this.permissionService.findByIds(permIds);
        role.changePermissions(perms);
        this.jpaRepository.save(role);
    }


    /**
     * 通过名称搜索
     *
     * @param names names
     * @return 返回角色
     */
    public Collection<Role> searchByNames(Collection<String> names) {
        Iterable<Role> tmp = this.querydslRepository.findAll(R.name.in(names));
        return CollectionUtils.convertToCollection(tmp);
    }
}
