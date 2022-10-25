package cn.procsl.ping.boot.captcha.domain.email;

import cn.procsl.ping.boot.captcha.domain.CaptchaSpecification;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.*;

public class EmailCaptchaSpec extends CaptchaSpecification<EmailCaptcha> {

    final String email;

    public EmailCaptchaSpec(String target, String email) {
        super(target);
        this.email = email;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<EmailCaptcha> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate tmp = super.toPredicate(root, query, cb);
        Path<String> condition = root.get("email");
        return cb.and(tmp, cb.equal(condition, this.email));
    }

}
