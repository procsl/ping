package cn.procsl.ping.boot.system.domain.session;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class SessionSpecification implements Specification<Session> {

    final String sessionId;

    final SessionState state;

    @Override
    public Predicate toPredicate(@NonNull Root<Session> root, @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        Predicate cond1 = cb.equal(root.get("sessionId"), sessionId);
        Predicate cond2 = cb.equal(root.get("state"), state);
        return cb.and(cond1, cond2);
    }

}
