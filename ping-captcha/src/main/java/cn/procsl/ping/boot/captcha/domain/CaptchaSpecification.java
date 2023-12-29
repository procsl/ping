package cn.procsl.ping.boot.captcha.domain;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

@RequiredArgsConstructor
public class CaptchaSpecification<C extends Captcha> implements Specification<C> {

    final String target;

    @Override
    public Predicate toPredicate(Root<C> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        val target = root.<String>get("target");
        val expired = root.<Date>get("expiredDate");
        val created = root.<Date>get("createDate");
        val times = root.<Integer>get("times");
        val verified = root.<Boolean>get("verified");
        Date now = new Date();

        return query.where(cb.equal(target, this.target),
                            cb.le(times, Captcha.max_times),
                            cb.greaterThanOrEqualTo(expired, now),
                            cb.lessThanOrEqualTo(created, now),
                            cb.equal(verified, Boolean.FALSE))
                    .orderBy(cb.desc(expired))
                    .getRestriction();
    }
}
