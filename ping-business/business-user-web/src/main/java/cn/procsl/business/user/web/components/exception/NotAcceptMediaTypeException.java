package cn.procsl.business.user.web.components.exception;

import cn.procsl.business.exception.BusinessException;

/**
 * @author procsl
 * @date 2020/01/03
 */
public class NotAcceptMediaTypeException extends BusinessException {
    @Override
    public Integer httpStatus() {
        return 416;
    }

    public NotAcceptMediaTypeException(String message, String code) {
        super(message, code);
    }

    public NotAcceptMediaTypeException(String message) {
        super(message);
    }
}
