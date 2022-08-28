package cn.procsl.ping.boot.captcha.service;

import cn.procsl.ping.boot.captcha.domain.Captcha;
import cn.procsl.ping.boot.captcha.domain.TicketSpecification;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Indexed
@Service
@RequiredArgsConstructor
public class VerifyCaptchaService {

    final JpaSpecificationExecutor<Captcha> specificationExecutor;

    final JpaRepository<Captcha, Long> jpaRepository;

    @Transactional(noRollbackFor = VerifyFailureException.class)
    public void verify(String id, String ticket, VerifyCaptcha markup) throws VerifyFailureException {
        if (id == null || id.isBlank() || ticket == null || ticket.isBlank()) {
            throw new VerifyFailureException("请输入%s验证码", markup.type().message);
        }

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createDate")));
        val page = this.specificationExecutor.findAll(new TicketSpecification(id, markup.type()), pageable);
        Optional<Captcha> captcha = page.get().findFirst();
        captcha.orElseThrow(() -> new VerifyFailureException("%s验证码错误", markup.type().message));
        captcha.ifPresent(this.jpaRepository::delete);
        captcha.ifPresent(item -> item.verify(ticket));
    }

}
