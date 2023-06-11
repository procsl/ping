package cn.procsl.ping.boot.web.encrypt;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "DECRYPT_ERROR")
public class DecryptException extends RuntimeException {

    String source;

    public DecryptException(String source, String message, Throwable e) {
        super(message, e);
    }
}
