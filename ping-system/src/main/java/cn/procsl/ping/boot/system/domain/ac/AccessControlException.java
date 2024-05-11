package cn.procsl.ping.boot.system.domain.ac;

/**
 * 访问控制异常
 */
public class AccessControlException extends RuntimeException {

    public AccessControlException(String message) {
        super(message);
    }

    public AccessControlException(String message, Exception e) {
        super(message, e);
    }
}
