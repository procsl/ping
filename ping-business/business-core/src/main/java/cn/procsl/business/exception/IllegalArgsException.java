package cn.procsl.business.exception;

/**
 * 不合法的数据/参数/实体
 *
 * @author procsl
 * @date 2019/12/13
 */
public class IllegalArgsException extends BusinessException {

    public IllegalArgsException() {
    }

    public IllegalArgsException(String message) {
        super(message);
    }

    public IllegalArgsException(String message, String code) {
        super(message, code);
    }
}
