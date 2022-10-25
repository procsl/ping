package cn.procsl.ping.boot.captcha.adapter;

import cn.procsl.ping.boot.captcha.domain.email.EmailCaptcha;

public interface EmailSenderAdapter {

    void sendEmailCaptcha(EmailCaptcha emailCaptcha);
}
