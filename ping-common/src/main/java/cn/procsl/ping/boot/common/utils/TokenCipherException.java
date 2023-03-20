package cn.procsl.ping.boot.common.utils;

public class TokenCipherException extends RuntimeException {

    public TokenCipherException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenCipherException(Throwable cause) {
        super(cause);
    }
}
