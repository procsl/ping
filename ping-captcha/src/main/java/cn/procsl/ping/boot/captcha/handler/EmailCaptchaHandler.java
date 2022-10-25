package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.adapter.EmailSenderAdapter;
import cn.procsl.ping.boot.captcha.domain.CaptchaSpecification;
import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import cn.procsl.ping.boot.captcha.domain.email.EmailCaptcha;
import cn.procsl.ping.boot.captcha.domain.email.EmailCaptchaSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Indexed
@Service
@RequiredArgsConstructor
public class EmailCaptchaHandler implements VerifyCaptchaHandler<VerifyCaptchaCommand> {

    final JpaRepository<EmailCaptcha, Long> emailCaptchaJpaRepository;

    final JpaSpecificationExecutor<EmailCaptcha> jpaSpecificationExecutor;

    final EmailSenderAdapter emailSenderAdapter;

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = VerifyFailureException.class)
    public void verify(VerifyCaptchaCommand context) throws VerifyFailureException {
        val all = this.jpaSpecificationExecutor.findAll(new CaptchaSpecification<>(context.target()));
        if (all.isEmpty()) {
            log.info("找不到绑定的邮件验证码信息");
            throw new VerifyFailureException("%s验证码已过期", CaptchaType.email.message);
        }

        EmailCaptcha first = all.get(0);
        first.verify(context.ticket());
        log.debug("邮件验证码验证成功:{}", first.getTicket());
    }

    @Transactional(rollbackFor = Exception.class)
    public void createEmailCaptcha(String target, String email) {
        val all = this.jpaSpecificationExecutor.findAll(new EmailCaptchaSpec(target, email));
        this.emailCaptchaJpaRepository.deleteAll(all);
        EmailCaptcha emailCaptcha = new EmailCaptcha(target, email);
        emailSenderAdapter.sendEmailCaptcha(emailCaptcha);
        emailCaptchaJpaRepository.save(emailCaptcha);
    }


}
