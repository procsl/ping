package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.domain.VerifyCaptchaCommand;
import cn.procsl.ping.boot.captcha.domain.email.EmailCaptcha;

public class SimpleMockCaptchaCommand implements VerifyCaptchaCommand {
    final EmailCaptcha emailCaptcha;

    public SimpleMockCaptchaCommand(EmailCaptcha emailCaptcha) {
        this.emailCaptcha = emailCaptcha;
    }

    @Override
    public String getClientId() {
        return emailCaptcha.getClientId();
    }

    @Override
    public String getClientTicket() {
        return emailCaptcha.getTicket();
    }
}
