package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.user.domain.common.service.AbstractService;
import cn.procsl.ping.boot.user.domain.rbac.model.Permission;
import cn.procsl.ping.boot.user.domain.rbac.model.QPermission;
import cn.procsl.ping.boot.user.domain.rbac.model.Target;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.jpa.JPAExpressions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

@Slf4j
@Validated
@Transactional(rollbackFor = Exception.class, readOnly = true)
public class PermissionService extends AbstractService<Long, Permission> {

    public static final QPermission P = QPermission.permission;

    final EntityManager entityManager;

    @Inject
    public PermissionService(JpaRepository<Permission, Long> jpaRepository,
                             QuerydslPredicateExecutor<Permission> querydslRepository,
                             EntityManager entityManager
    ) {
        super(jpaRepository, querydslRepository);
        this.entityManager = entityManager;
    }

    /**
     * 通过target获取
     *
     * @param targets target 值对象
     * @return 返回实体
     */
    public Collection<Permission> getByTargets(Collection<? extends Target> targets) {
        if (CollectionUtils.isEmpty(targets)) {
            return Collections.emptyList();
        }

        TypedQuery<Permission> query = entityManager.createQuery(template(targets), Permission.class);
        return parameter(targets, query).getResultList();
    }

    private TypedQuery<Permission> parameter(Collection<? extends Target> targets, TypedQuery<Permission> query) {

        Iterator<? extends Target> iter = targets.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Target curr = iter.next();
            query.setParameter(String.format("target_resource_%d", i), curr.getResource());
            query.setParameter(String.format("target_operator_%d", i), curr.getOperator());
            i++;
        }

        return query;
    }

    private String template(Collection<? extends Target> collection) {

        ArrayList<String> list = new ArrayList<>(collection.size());

        final String tmp = "(:target_resource_%d, :target_operator_%d)";
        for (int i = 0; i < collection.size(); i++) {
            list.add(String.format(tmp, i, i));
        }
        String params = String.join(",", list);

        String jpql = JPAExpressions.select(P).from(P).where(ExpressionUtils.in(ExpressionUtils.list(Target.class, P.resource, P.operator), Collections.emptyList())).toString();
        return jpql.replace("?1", "(" + params + ")");

    }

    public <T extends Target> Long create(@NotNull @Valid T target) {
        return this.jpaRepository.save(new Permission(target)).getId();
    }

    public Long create(@NotEmpty String resource, @NotEmpty String operator) {
        return this.jpaRepository.save(new Permission(resource, operator)).getId();
    }
}
