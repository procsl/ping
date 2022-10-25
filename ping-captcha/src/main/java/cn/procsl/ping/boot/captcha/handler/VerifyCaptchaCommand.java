package cn.procsl.ping.boot.captcha.handler;

public interface VerifyCaptchaCommand {

    String target();

    String ticket();

}
