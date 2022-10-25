package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.domain.email.EmailCaptcha;

public class SimpleMockCaptchaCommand implements VerifyCaptchaCommand {
    final EmailCaptcha emailCaptcha;

    public SimpleMockCaptchaCommand(EmailCaptcha emailCaptcha) {
        this.emailCaptcha = emailCaptcha;
    }

    @Override
    public String target() {
        return emailCaptcha.getTarget();
    }

    @Override
    public String ticket() {
        return emailCaptcha.getTicket();
    }
}
