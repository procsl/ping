package cn.procsl.ping.boot.captcha.service;

import cn.procsl.ping.boot.captcha.domain.*;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;

@Indexed
@Component
@RequiredArgsConstructor
public class GeneratedCaptchaService implements CaptchaGenerator {

    final JpaRepository<Captcha, Long> jpaRepository;

    final JpaSpecificationExecutor<Captcha> specificationExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generated(String id, CaptchaType type, OutputStream os) {
        List<Captcha> all = this.specificationExecutor.findAll(new TicketSpecification(id, type));
        this.jpaRepository.deleteAll(all);
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 6);
        ImageCaptcha imageCaptcha = new ImageCaptcha(id, specCaptcha.text(), 2);
        this.jpaRepository.save(imageCaptcha);
        specCaptcha.out(os);
    }
}
