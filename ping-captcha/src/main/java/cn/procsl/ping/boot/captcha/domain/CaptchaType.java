package cn.procsl.ping.boot.captcha.domain;


public enum CaptchaType {
    image("图形"),
    sms("短信"),
    email("邮箱"),
    dynamic_code("动态");

    public final String message;

    CaptchaType(String message) {
        this.message = message;
    }
}
