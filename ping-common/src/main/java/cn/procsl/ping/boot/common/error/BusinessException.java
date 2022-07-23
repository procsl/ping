package cn.procsl.ping.boot.common.error;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author procsl
 * @date 2020/05/15
 */
public class BusinessException extends RuntimeException implements ErrorEntity {

    @Getter
    Integer httpStatus = 501;

    @Getter
    String code = "001";

    public BusinessException(String format, Object... arguments) {
        super(String.format(format, arguments), null, true, true);
    }

    public BusinessException(String code, Integer httpStatus, String format, Object... arguments) {
        super(String.format(format, arguments), null, true, true);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
