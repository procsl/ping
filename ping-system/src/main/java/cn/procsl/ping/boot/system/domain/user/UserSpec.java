package cn.procsl.ping.boot.system.domain.user;

import jakarta.persistence.criteria.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class UserSpec implements Specification<User> {

    final String accountName;

    @Override
    public Predicate toPredicate(Root<User> root, @NonNull CriteriaQuery<?> query, CriteriaBuilder cb) {
        Join<User, Account> account = root.join("account", JoinType.INNER);
        return cb.equal(account.get("name"), accountName);
    }

}
