package cn.procsl.ping.boot.admin.domain.session;

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

    @Override
    public Predicate toPredicate(@NonNull Root<Session> root, @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        return cb.equal(root.get("sessionId"), sessionId);
    }

}
