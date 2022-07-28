package cn.procsl.ping.boot.admin.domain.rbac;

import cn.procsl.ping.boot.common.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Indexed
@Service
@RequiredArgsConstructor
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

        log.debug("subject is {}, role name is {}.", subject, roleNames);
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
            Set<String> dbRoles = roles.stream().map(Role::getName).collect(Collectors.toSet());
            Set<String> params = roleNames.stream().filter(item -> !dbRoles.contains(item)).collect(Collectors.toSet());
            if (!params.isEmpty()) {
                throw new BusinessException("角色不存在: [%s]", String.join(",", params));
            }
        }

        save(subject, roles);
    }

    private void save(Long subject, List<Role> roles) {
        Optional<Subject> option = this.subjectJpaSpecificationExecutor.findOne(
                (root, query, cb) -> cb.equal(root.get("subject"), subject));
        Subject entity = option.orElseGet(Subject::new);
        entity.setSubject(subject);
        entity.putRoles(roles);
        this.subjectRepository.save(entity);
    }

    /**
     * 判断是否存在角色
     *
     * @param subject 目标
     * @param role    角色名称
     * @return 如果存在角色返回true
     */
    @Transactional(readOnly = true)
    public boolean hasRole(@NotNull Long subject, @NotEmpty String role) {
        long result = this.subjectJpaSpecificationExecutor.count((root, query, cb) -> {
            Join<Subject, Role> join = root.join("roles", JoinType.INNER);
            Predicate condition1 = cb.equal(root.get("subject"), subject);
            Predicate condition2 = cb.equal(join.get("name"), role);
            return cb.and(condition1, condition2);
        });
        return result > 0;
    }

    /**
     * @param subject 目标对象
     * @return 加载指定subject的所有权限
     */
    @Transactional(readOnly = true)
    public Collection<Permission> loadPermissions(Long subject) {
        Collection<Role> roles = this.loadRoles(subject);
        HashSet<Permission> hashset = new HashSet<>();
        for (Role role : roles) {
            hashset.addAll(role.getPermissions());
        }
        return Collections.unmodifiableCollection(hashset);
    }

    /**
     * 加载权限, 并转换
     *
     * @param subject 目标对象
     * @param convert 转换器
     * @param <T>     转换类型
     * @return 返回被转换后的类型
     */
    @Transactional(readOnly = true)
    public <T> Collection<T> loadPermissions(Long subject, Function<Permission, T> convert) {
        Collection<Permission> tmp = this.loadPermissions(subject);
        return tmp.stream().map(convert).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    /**
     * @param subject 目标对象
     * @return 加载所有角色
     */
    @Transactional(readOnly = true)
    public Collection<Role> loadRoles(Long subject) {
        Optional<Subject> entity = this.subjectJpaSpecificationExecutor.findOne(
                (root, query, cb) -> cb.equal(root.get("subject"), subject));
        if (entity.isEmpty()) {
            return Collections.emptyList();
        }
        return entity.get().getRoles();
    }

}
