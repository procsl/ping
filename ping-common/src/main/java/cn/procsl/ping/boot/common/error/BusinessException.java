package cn.procsl.ping.boot.common.error;

import lombok.Getter;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 业务异常
 *
 * @author procsl
 * @date 2020/05/15
 */
//@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "SYSTEM_ERROR")
public class BusinessException extends RuntimeException implements ErrorEntity {

    @Getter
    String code = "SYSTEM_ERROR";

    public BusinessException(String message) {
        super(message, null, true, true);
    }

    public BusinessException(String message, Exception e) {
        super(message, e, true, true);
    }

}
