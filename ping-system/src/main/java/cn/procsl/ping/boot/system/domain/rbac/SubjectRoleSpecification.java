package cn.procsl.ping.boot.system.domain.rbac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.*;

@RequiredArgsConstructor
public class SubjectRoleSpecification implements Specification<Subject> {

    final Long subjectId;
    final String roleName;

    @Override
    public Predicate toPredicate(@NonNull Root<Subject> root, @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {

        Predicate condition1 = cb.equal(root.get("subject"), subjectId);
        if (roleName == null) {
            return condition1;
        }
        Join<Subject, Role> join = root.join("roles", JoinType.INNER);
        Predicate condition2 = cb.equal(join.get("name"), roleName);
        return cb.and(condition1, condition2);
    }

}
