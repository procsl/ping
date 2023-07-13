package cn.procsl.ping.boot.web.cipher;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "DECRYPT_ERROR")
public class CipherException extends RuntimeException {

    final String source;

    public CipherException(String source, String message, Throwable e) {
        super(message, e);
        this.source = source;
    }
}
