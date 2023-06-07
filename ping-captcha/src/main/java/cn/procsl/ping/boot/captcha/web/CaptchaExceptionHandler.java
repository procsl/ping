package cn.procsl.ping.boot.captcha.web;

import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import cn.procsl.ping.boot.common.dto.MessageVO;
import cn.procsl.ping.boot.common.error.ErrorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CaptchaExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = VerifyFailureException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public MessageVO exceptionHandler(VerifyFailureException e) {
        return ErrorVO.build(e);
    }

}
