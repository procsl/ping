package cn.procsl.ping.boot.captcha.domain;

import cn.procsl.ping.boot.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class VerifyFailureException extends BusinessException {

    boolean ticketError;

    public VerifyFailureException(String format, Object... arguments) {
        super(HttpStatus.FORBIDDEN, "003", format, arguments);
        this.ticketError = false;
    }

    public VerifyFailureException(boolean ticketError, String format, Object... arguments) {
        super(HttpStatus.FORBIDDEN, "003", format, arguments);
        this.ticketError = ticketError;
    }

    public VerifyFailureException(Exception e, boolean ticketError, String format, Object... arguments) {
        super(e, HttpStatus.FORBIDDEN, "003", format, arguments);
        this.ticketError = ticketError;
    }

    public boolean isTicketError() {
        return ticketError;
    }

}
