package cn.procsl.ping.boot.captcha.domain;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@RequiredArgsConstructor
public class TicketSpecification implements Specification<Captcha> {

    final String target;

    final CaptchaType type;


    @Override
    public Predicate toPredicate(@NonNull Root<Captcha> root, @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        Root<? extends Captcha> node = query.from(type.captcha);
        val target = node.get("target");
        val expired = node.get("expired");
        return query.where(cb.equal(target, this.target))
                    .orderBy(cb.desc(expired))
                    .getRestriction();
    }
}
