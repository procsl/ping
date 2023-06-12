package cn.procsl.ping.boot.system.domain.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@RequiredArgsConstructor
public class AuthenticationSpecification implements Specification<Authentication> {

    final String sessionId;

    final AuthenticationState state;

    @Override
    public Predicate toPredicate(@NonNull Root<Authentication> root, @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        Predicate cond1 = cb.equal(root.get("sessionId"), sessionId);
        Predicate cond2 = cb.equal(root.get("state"), state);
        return cb.and(cond1, cond2);
    }

}
