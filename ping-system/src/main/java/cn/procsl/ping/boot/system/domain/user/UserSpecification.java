package cn.procsl.ping.boot.system.domain.user;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

@RequiredArgsConstructor
public class UserSpecification implements Specification<User> {

    final String accountName;

    @Override
    public Predicate toPredicate(Root<User> root, @NonNull CriteriaQuery<?> query, CriteriaBuilder cb) {
        Join<User, Account> account = root.join("account", JoinType.INNER);
        return cb.equal(account.get("name"), accountName);
    }

}
