package cn.procsl.ping.boot.captcha.service;

import cn.procsl.ping.boot.captcha.domain.Captcha;
import cn.procsl.ping.boot.captcha.domain.CaptchaGenerator;
import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import com.wf.captcha.GifCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.io.OutputStream;

@Slf4j
@Indexed
@Component
@RequiredArgsConstructor
public class GeneratedCaptchaService implements CaptchaGenerator {

    final JpaRepository<Captcha, Long> jpaRepository;

    final JpaSpecificationExecutor<Captcha> specificationExecutor;

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void generated(String id, CaptchaType type, OutputStream os) {
//        List<Captcha> all = this.specificationExecutor.findAll(new TicketSpecification(id, type));
//        this.jpaRepository.deleteAll(all);
        GifCaptcha specCaptcha = new GifCaptcha(130, 48, 6);
//        ImageCaptcha imageCaptcha = new ImageCaptcha(id, specCaptcha.text(), 2);
//        this.jpaRepository.save(imageCaptcha);
//        log.debug("Unique id:{} ImageCaptcha:{}",id, imageCaptcha);
        specCaptcha.out(os);
    }
}
