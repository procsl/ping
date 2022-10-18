package cn.procsl.ping.boot.captcha.domain;


import cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha;

public enum CaptchaType {
    image("图形", ImageCaptcha.class),
    sms("短信", Captcha.class),
    email("邮箱", Captcha.class),
    dynamic_code("动态", Captcha.class);

    public final String message;

    public final Class<? extends Captcha> captcha;

    CaptchaType(String message, Class<? extends Captcha> captcha) {
        this.message = message;
        this.captcha = captcha;
    }
}
