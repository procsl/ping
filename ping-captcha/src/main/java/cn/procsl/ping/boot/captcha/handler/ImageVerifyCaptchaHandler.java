package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaBuilderService;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
class ImageVerifyCaptchaHandler implements VerifyCaptchaHandler<VerifyCaptchaCommand> {

    final ImageCaptchaBuilderService imageCaptchaBuilderService = new ImageCaptchaBuilderService();


    final ImageCaptchaRepository imageCaptchaRepository;

    @Override
    public void verify(VerifyCaptchaCommand command) throws VerifyFailureException {
        assert command instanceof ImageVerifyCommand;
        ImageVerifyCommand context = (ImageVerifyCommand) command;

        String token = context.token();
        if (ObjectUtils.isEmpty(token)) {
            log.warn("图形验证码 token 为 null");
            throw new VerifyFailureException("图形验证码已过期");
        }

        ImageCaptcha imageCaptcha;
        try {
            imageCaptcha = imageCaptchaBuilderService.buildForToken(context.key(), token);
        } catch (IOException e) {
            log.warn("图形验证码解码失败", e);
            throw new VerifyFailureException("图形验证码错误");
        }

        if (imageCaptcha == null) {
            throw new VerifyFailureException("图形验证码错误");
        }

        assert imageCaptcha.getId() != null;
        String ticket = context.ticket();

        try {
            imageCaptcha.verify(ticket);
        } catch (VerifyFailureException e) {
            if (e.isTicketError()) {
                // 如果是验证码本身错误, 需要保存防止爆破
                this.imageCaptchaRepository.save(imageCaptcha);
            }
            throw e;
        }

        ImageCaptcha db = this.imageCaptchaRepository.findById(imageCaptcha.getId());
        if (db == null) {
            this.imageCaptchaRepository.save(imageCaptcha);
            return;
        }

        // 再次校验并保存
        db.verify(ticket);
        this.imageCaptchaRepository.save(db);
    }


}
