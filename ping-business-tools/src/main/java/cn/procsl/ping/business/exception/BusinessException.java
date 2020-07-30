package cn.procsl.ping.business.exception;

/**
 * 业务异常
 *
 * @author procsl
 * @date 2020/05/15
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String format, Object... arguments) {
        // TODO 这里应该用占位符的, 先写着
        super(format);
    }
}
