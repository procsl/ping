package cn.procsl.ping.boot.infra.domain.rbac;

import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.criteria.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Indexed
@Service
@RequiredArgsConstructor
@Validated
public class AccessControlService {

    final JpaRepository<Subject, Long> subjectRepository;

    final JpaSpecificationExecutor<Role> roleJpaSpecificationExecutor;

    final JpaSpecificationExecutor<Subject> subjectJpaSpecificationExecutor;


    /**
     * 分配角色
     *
     * @param subject   目标对象信息
     * @param roleNames 角色名称
     * @throws BusinessException 如果角色不存在
     */
    @Transactional
    public void grant(@NotNull Long subject, @NotNull Collection<String> roleNames) throws BusinessException {

        if (roleNames.isEmpty()) {
            save(subject, Collections.emptyList());
            return;
        }

        List<Role> roles = this.roleJpaSpecificationExecutor.findAll((root, query, cb) -> {
            CriteriaBuilder.In<String> in = cb.in(root.get("name"));
            roleNames.forEach(in::value);
            return in;
        });

        // 角色数量不同, 检测具体的角色并报错
        if (roles.size() < roleNames.size()) {
            // TODO 优化
            Set<String> set = roles.stream().map(Role::getName).collect(Collectors.toSet());
            HashSet<String> names = new HashSet<>(roleNames);
            names.removeAll(set);
            throw new BusinessException("角色不存在");
        }

        save(subject, roles);
    }

    void save(Long subject, List<Role> roles) {
        Optional<Subject> option = this.subjectJpaSpecificationExecutor.findOne((root, query, cb) -> cb.equal(root.get("subject"), subject));
        Subject entity = option.orElseGet(Subject::new);
        entity.setSubject(subject);
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
    @Transactional(readOnly = true)
    public boolean hasPermission(@NotNull Long subject, @NotEmpty String permission) {
        long result = this.subjectJpaSpecificationExecutor.count((root, query, cb) -> {
            Join<Subject, Role> join = root.join("roles", JoinType.INNER);
            SetJoin<Role, Permission> joinPermissions = join.joinSet("permissions", JoinType.INNER);

            Predicate condition1 = cb.equal(root.get("subject"), subject);
            Predicate condition2 = cb.equal(joinPermissions.get("name"), permission);
            return cb.and(condition1, condition2);
        });
        return result > 0;
    }

    /**
     * 判断是否存在角色
     *
     * @param subject 目标
     * @param role    角色名称
     * @return 如果存在角色返回true
     */
    public boolean hasRole(@NotNull Long subject, @NotEmpty String role) {
        long result = this.subjectJpaSpecificationExecutor.count((root, query, cb) -> {
            Join<Subject, Role> join = root.join("roles", JoinType.INNER);
            Predicate condition1 = cb.equal(root.get("subject"), subject);
            Predicate condition2 = cb.equal(join.get("name"), role);
            return cb.and(condition1, condition2);
        });
        return result > 0;
    }

}
