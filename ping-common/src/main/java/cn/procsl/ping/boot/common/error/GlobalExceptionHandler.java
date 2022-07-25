package cn.procsl.ping.boot.common.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorCode exceptionHandler(Exception e) {
        log.warn("未解析的异常:", e);
//        if (handlerMethod == null) {
//            return getDefaultErrorCode(e);
//        }
//        ExceptionResolver resolver = handlerMethod.getMethod().getAnnotation(ExceptionResolver.class);
//        if (resolver == null) {
//            return getDefaultErrorCode(e);
//        }
//        if (e.getClass().isAssignableFrom(resolver.matcher())) {
//            return ErrorCode.builder(resolver.code(), resolver.message());
//        }
        return getDefaultErrorCode(e);
    }

    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public ErrorCode BusinessExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                              BusinessException businessException) {
        response.setStatus(businessException.getHttpStatus());
        return ErrorCode.builder(businessException.getHttpStatus() + businessException.getCode(),
                businessException.getMessage());
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ParameterErrorCode MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        return new ParameterErrorCode("400001", "参数校验失败", Collections.emptyList());
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ErrorCode httpMessageNotReadableException() {
        return ErrorCode.builder("400001", "请求体不可为空");
    }

    ErrorCode getDefaultErrorCode(Exception e) {
        if (log.isDebugEnabled()) {
            return ErrorCode.builder("E001", e.getMessage());
        }
        return ErrorCode.builder("E001", "server error!");
    }

}
