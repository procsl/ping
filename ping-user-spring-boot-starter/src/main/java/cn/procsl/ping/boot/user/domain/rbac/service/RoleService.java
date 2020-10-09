package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.domain.business.state.repository.BooleanStatefulRepository;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.PathUtils;
import cn.procsl.ping.boot.domain.business.utils.StringUtils;
import cn.procsl.ping.boot.user.domain.common.AbstractBooleanStatefulService;
import cn.procsl.ping.boot.user.domain.common.AbstractTreeService;
import cn.procsl.ping.boot.user.domain.rbac.model.*;
import com.querydsl.core.types.Predicate;
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
import java.util.List;
import java.util.function.Function;

import static cn.procsl.ping.boot.user.domain.rbac.model.Role.ROLE_NAME_LEN;
import static cn.procsl.ping.business.exception.BusinessException.ifNotFound;

/**
 * 角色相关的业务,应该做拦截做相关的事件处理
 */
@Slf4j
@Validated
@Transactional(rollbackFor = Exception.class)
public class RoleService extends AbstractTreeService<Role, Long, Node>
    implements AbstractBooleanStatefulService<Role, Long> {

    public static final QRole R = QRole.role;
    @Getter
    final BooleanStatefulRepository<Role, Long> booleanStatefulRepository;

    @Inject
    PermissionService permissionService;

    @Inject
    public RoleService(AdjacencyTreeRepository<Role, Long, Node> currentTreeRepository,
                       QuerydslPredicateExecutor<Role> querydslRepository,
                       PermissionService permissionService,
                       JpaRepository<Role, Long> jpaRepository,
                       BooleanStatefulRepository<Role, Long> booleanStatefulRepository
    ) {
        super(jpaRepository, querydslRepository, currentTreeRepository);
        this.permissionService = permissionService;
        this.booleanStatefulRepository = booleanStatefulRepository;
    }

    /**
     * 创建角色
     *
     * @param name       角色名称
     * @param parentPath 父角色路径, 可为空, 如果为空则表示不继承
     * @return 角色创建成功之后返回角色ID
     * @throws IllegalArgumentException 如果父角色,或者权限不存在则抛出此异常
     */
    @Transactional
    public Long create(@NotBlank @Size(min = 1, max = ROLE_NAME_LEN) String name,
                       String parentPath, List<? extends Target> targets) throws EntityNotFoundException {

        Role parent = null;
        if (!StringUtils.isEmpty(parentPath)) {
            parent = this.searchOne(parentPath);
            ifNotFound(parent, parentPath);
        }

        Collection<Permission> permissions = permissionService.getByTargets(targets);

        Role role = jpaRepository.save(new Role(name, parent, permissions));
        return role.getId();
    }

    /**
     * 创建角色,通过指定父角色和权限ID
     *
     * @param name          角色名称
     * @param parentId      父角色ID
     * @param permissionIds 权限IDs
     * @return 创建成功返回角色ID
     * @throws EntityNotFoundException 如果权限IDs,或父角色ID未找到
     */
    @Transactional
    public Long create(@NotBlank @Size(min = 1, max = ROLE_NAME_LEN) String name,
                       Long parentId, Collection<Long> permissionIds) throws EntityNotFoundException {
        Role parent = this.findById(parentId);
        Collection<Permission> perms = this.permissionService.findByIds(permissionIds);
        Role role = jpaRepository.saveAndFlush(new Role(name, parent, perms));
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
        Role role = this.treeRepository.findOne(R, R.id.eq(roleId).and(R.name.ne(name)));
        if (role == null) {
            return;
        }
        role.setName(name);
        this.jpaRepository.save(role);
    }

    /**
     * 修改继承的角色
     *
     * @param roleId   被修改的角色ID
     * @param parentId 父角色的ID
     * @throws EntityNotFoundException 如果当前的角色id不存在对应的实体, 或者parentId不为null且不存在对应的实体, 则抛出此异常
     */
    public void changeParent(@NotNull Long roleId, Long parentId) throws EntityNotFoundException {
        this.treeRepository.mount(parentId, roleId);
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
     * 搜索一个节点
     *
     * @param path      指定的path
     * @param predicate 指定的条件
     * @return 返回搜索到的节点
     * @throws IllegalArgumentException 如果条件参数错误
     */
    @Override
    public Role searchOne(@NotBlank String path, Function<Integer, Predicate> predicate) throws IllegalArgumentException {
        List<String> nodes = PathUtils.split(path, Role.DELIMITER);
        return treeRepository.searchOne(R, nodes, merge(predicate, nodes), true);
    }

    Function<Integer, Predicate> merge(Function<Integer, Predicate> predicate, List<String> nodes) {
        if (predicate != null) {
            return (index) -> R.name.eq(nodes.get(index)).and(predicate.apply(index));
        } else {
            return (index) -> R.name.eq(nodes.get(index));
        }
    }

}
