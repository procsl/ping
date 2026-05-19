package cn.procsl.ping.boot.system.domain.rbac;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class SubjectRoleSpec implements Specification<Subject> {

    final Long subjectId;
    final String roleName;

    @Override
    public Predicate toPredicate(Root<Subject> root, @NonNull CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate condition1 = cb.equal(root.get("subject"), subjectId);
        if (roleName == null) {
            return condition1;
        }
        Join<Subject, Role> join = root.join("roles", JoinType.INNER);
        Predicate condition2 = cb.equal(join.get("name"), roleName);
        return cb.and(condition1, condition2);
    }
}
