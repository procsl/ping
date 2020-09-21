package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.PathUtils;
import cn.procsl.ping.boot.user.domain.common.AbstractTreeService;
import cn.procsl.ping.boot.user.domain.rbac.model.Node;
import cn.procsl.ping.boot.user.domain.rbac.model.Operator;
import cn.procsl.ping.boot.user.domain.rbac.model.Permission;
import cn.procsl.ping.boot.user.domain.rbac.model.QPermission;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.function.Function;

import static cn.procsl.ping.boot.user.domain.rbac.model.Permission.*;

@Slf4j
@Validated
@Transactional(rollbackFor = Exception.class, readOnly = true)
public class PermissionService
    extends AbstractTreeService<Permission, Long, Node> {

    public static final QPermission P = QPermission.permission;

    public PermissionService(JpaRepository<Permission, Long> jpaRepository,
                             QuerydslPredicateExecutor<Permission> querydslRepository,
                             AdjacencyTreeRepository<Permission, Long, Node> treeRepository) {
        super(jpaRepository, querydslRepository, treeRepository);
    }

    @Transactional(readOnly = true)
    @Override
    public Permission searchOne(@NotBlank String path, Function<Integer, Predicate> predicate) throws IllegalArgumentException {
        List<String> nodes = PathUtils.split(path, Permission.DELIMITER);
        return treeRepository.searchOne(P, nodes, this.merge(predicate, nodes), true);
    }

    Function<Integer, Predicate> merge(Function<Integer, Predicate> predicate, List<String> nodes) {
        if (predicate != null) {
            return (index) -> P.name.eq(nodes.get(index)).and(predicate.apply(index));
        } else {
            return (index) -> P.name.eq(nodes.get(index));
        }
    }

    /**
     * 创建权限
     *
     * @param parentId 父权限ID
     * @param name     权限名称
     * @param type     权限类型
     * @param target   权限目标
     * @param operator 权限支持的操作
     * @return 创建成功返回权限ID
     */
    @Transactional
    public Long create(Long parentId,
                       @NotBlank @Size(min = 1, max = PERM_NAME_LEN) String name,
                       @NotBlank @Size(min = 1, max = PERM_TYPE_LEN) String type,
                       @NotBlank @Size(min = 1, max = PERM_TARGET_LEN) String target,
                       @NotNull Operator operator
    ) {
        Permission parent = this.findById(parentId);
        Permission perm = this.jpaRepository.save(new Permission(name, type, target, operator, parent));
        return perm.getId();
    }
}
