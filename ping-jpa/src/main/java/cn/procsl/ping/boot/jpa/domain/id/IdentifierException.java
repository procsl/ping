package cn.procsl.ping.boot.jpa.support;

public class IdentifierException extends RuntimeException {


    public IdentifierException(String message) {
        super(message);
    }


    public IdentifierException(String message, Throwable cause) {
        super(message, cause);
    }
}
