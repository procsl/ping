package cn.procsl.ping.boot.system.domain.user;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class AuthenticateSpec implements Specification<Authenticate> {

    final String sessionId;

    final AuthenticateState state;

    @Override
    public Predicate toPredicate(@NonNull Root<Authenticate> root, @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        Predicate cond1 = cb.equal(root.get("sessionId"), sessionId);
        Predicate cond2 = cb.equal(root.get("state"), state);
        return cb.and(cond1, cond2);
    }

}
