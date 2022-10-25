package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;

public interface VerifyCaptchaHandler<C extends VerifyCaptchaCommand> {

    void verify(C context) throws VerifyFailureException;

}
