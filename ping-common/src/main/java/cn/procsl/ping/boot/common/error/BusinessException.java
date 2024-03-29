package cn.procsl.ping.boot.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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

    public BusinessException(HttpStatus httpStatus, String code, String format, Object... arguments) {
        super(String.format(format, arguments), null, true, true);
        this.code = code;
        this.httpStatus = httpStatus.value();
    }

    public BusinessException(Exception e, String format, Object... arguments) {
        super(String.format(format, arguments), e, true, true);
    }

    public BusinessException(Exception e, HttpStatus httpStatus, String code, String format, Object... arguments) {
        super(String.format(format, arguments), e, true, true);
        this.code = code;
        this.httpStatus = httpStatus.value();
    }
}
