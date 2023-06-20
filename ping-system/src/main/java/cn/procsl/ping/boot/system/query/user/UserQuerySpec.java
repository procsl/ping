package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.domain.user.Account;
import cn.procsl.ping.boot.system.domain.user.AccountState;
import cn.procsl.ping.boot.system.domain.user.Gender;
import cn.procsl.ping.boot.system.domain.user.User;
import jakarta.persistence.criteria.*;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Builder
public class UserQuerySpec implements Specification<User> {

    final String name;
    final String account;
    final AccountState state;
    final Gender gender;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        final List<Predicate> condition = new ArrayList<>();
        Join<User, Account> accountField = root.join("account", JoinType.INNER);

        if (StringUtils.hasText(name)) {
            Path<String> tmp = root.get("name");
            condition.add(cb.like(tmp, String.format("%%%s%%", name)));
        }

        if (StringUtils.hasText(account)) {
            Path<String> tmp = accountField.get("name");
            condition.add(cb.like(tmp, String.format("%%%s%%", account)));
        }

        if (gender != null) {
            Path<Gender> tmp = root.get("gender");
            condition.add(cb.equal(tmp, gender));
        }

        if (state != null) {
            Path<Gender> tmp = accountField.get("state");
            condition.add(cb.equal(tmp, gender));
        }

        return query.where(condition.toArray(Predicate[]::new)).getRestriction();
    }

}