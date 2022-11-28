package cn.procsl.ping.boot.system.domain.session;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
