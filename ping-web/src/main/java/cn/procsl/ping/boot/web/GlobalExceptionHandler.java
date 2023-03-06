package cn.procsl.ping.boot.web;

import cn.procsl.ping.boot.common.dto.MessageVO;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.error.ErrorVO;
import cn.procsl.ping.boot.common.error.ParameterErrorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.RollbackException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageVO exceptionHandler(Exception e) {
        log.warn("未解析的异常:", e);
        return new MessageVO("服务器内部错误");
    }

    @ResponseBody
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageVO runtimeExceptionHandler(RuntimeException e) {
        log.warn("未解析的异常:", e);
        return new MessageVO("服务器内部错误");
    }

    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public ErrorVO businessExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                            BusinessException businessException) {
        response.setStatus(businessException.getHttpStatus());
        return ErrorVO.builder(businessException.getHttpStatus() + businessException.getCode(),
                businessException.getMessage());
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ParameterErrorVO methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        HashMap<String, String> tmp = new HashMap<>();
        List<ObjectError> errors = result.getAllErrors();
        if (CollectionUtils.isEmpty(errors)) {
            log.warn("处理参数校验失败异常时, 找不到参数校验信息", e);
        }

        errors.forEach(error -> {
            if (error instanceof FieldError) {
                tmp.put(((FieldError) error).getField(), error.getDefaultMessage());
            } else {
                tmp.put(error.getObjectName(), error.getDefaultMessage());
            }
        });
        return new ParameterErrorVO("400001", "参数校验失败", tmp);
    }


    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ParameterErrorVO constraintViolationException(ConstraintViolationException constraintViolationException) {
        HashMap<String, String> tmp = new HashMap<>();
        for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
            String key = violation.getPropertyPath().toString();
            tmp.put(key, violation.getMessage());
        }
        return new ParameterErrorVO("400001", "参数校验失败", tmp);
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ErrorVO httpMessageNotReadableException() {
        return ErrorVO.builder("400001", "请求体不可为空");
    }

    ErrorVO getDefaultErrorCode(int code, Exception e) {
        String s = String.format("%s001", code);
        if (e == null) {
            return ErrorVO.builder(s, "server error!");
        }

        if (log.isDebugEnabled()) {
            return ErrorVO.builder(s, e.getMessage());
        }
        return ErrorVO.builder(s, "server error!");
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RollbackException.class)
    public ErrorVO rollbackException(RollbackException rollbackException) {
        Throwable cause = rollbackException.getCause();
        if (cause instanceof ConstraintViolationException) {
            return this.constraintViolationException((ConstraintViolationException) cause);
        }
        return ErrorVO.builder("400001", "参数校验失败");
    }

//    @ResponseBody
//    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(value = TransactionSystemException.class)
//    public ErrorVO transactionSystemException(TransactionSystemException transactionSystemException) {
//        if (!(transactionSystemException.getCause() instanceof RollbackException)) {
//            return getDefaultErrorCode(500, transactionSystemException);
//        }
//        return this.rollbackException((RollbackException) transactionSystemException.getCause());
//    }


    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ErrorVO httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return new ErrorVO("400003", exception.getMessage());
    }

}
