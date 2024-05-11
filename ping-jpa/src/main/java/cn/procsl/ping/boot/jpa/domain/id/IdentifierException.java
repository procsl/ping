package cn.procsl.ping.boot.jpa.domain.id;

public class IdentifierException extends RuntimeException {


    public IdentifierException(String message) {
        super(message);
    }


    public IdentifierException(String message, Throwable cause) {
        super(message, cause);
    }
}
