package cn.procsl.ping.boot.captcha.domain;

import cn.procsl.ping.boot.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class VerifyFailureException extends BusinessException {

    public VerifyFailureException(String format, Object... arguments) {
        super(HttpStatus.FORBIDDEN, "003", format, arguments);
    }

    public VerifyFailureException(Exception e, String format, Object... arguments) {
        super(e, HttpStatus.FORBIDDEN, "003", format, arguments);
    }

}
