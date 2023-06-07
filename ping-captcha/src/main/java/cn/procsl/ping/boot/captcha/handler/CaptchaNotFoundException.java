package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.common.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "CAPTCHA_NOT_FOUND")
public class CaptchaNotFoundException extends BusinessException {
    public CaptchaNotFoundException(String message) {
        super(message);
    }
}
