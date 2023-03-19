package cn.procsl.ping.boot.system.domain.user;

import cn.procsl.ping.boot.common.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticateException extends BusinessException {
    public AuthenticateException(String format, Object... arguments) {
        super(HttpStatus.UNAUTHORIZED.value(), "001", format, arguments);
    }
}
