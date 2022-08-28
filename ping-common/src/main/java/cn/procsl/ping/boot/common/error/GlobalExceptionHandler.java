package cn.procsl.ping.boot.common.error;

import cn.procsl.ping.boot.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.annotation.Nullable;
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
    public ErrorCode exceptionHandler(Exception e, @Nullable HandlerMethod handlerMethod) {
        log.warn("未解析的异常:", e);
        if (handlerMethod == null) {
            return getDefaultErrorCode(500, e);
        }

        ExceptionResolver resolver = ((handlerMethod).getMethod()).getAnnotation(
                ExceptionResolver.class);
        if (resolver == null) {
            return getDefaultErrorCode(500, e);
        }

        if (resolver.matcher().isAssignableFrom(e.getClass())) {
            return ErrorCode.builder(resolver.code(), resolver.message());
        }

        return getDefaultErrorCode(500, e);
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
        return new ParameterErrorCode("400001", "参数校验失败", tmp);
    }


    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ParameterErrorCode constraintViolationException(ConstraintViolationException constraintViolationException) {
        HashMap<String, String> tmp = new HashMap<>();
        for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
            String key = violation.getPropertyPath().toString();
            tmp.put(key, violation.getMessage());
        }
        return new ParameterErrorCode("400001", "参数校验失败", tmp);
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ErrorCode httpMessageNotReadableException() {
        return ErrorCode.builder("400001", "请求体不可为空");
    }

    ErrorCode getDefaultErrorCode(int code, Exception e) {
        String s = String.format("%s001", code);
        if (e == null) {
            return ErrorCode.builder(s, "server error!");
        }

        if (log.isDebugEnabled()) {
            return ErrorCode.builder(s, e.getMessage());
        }
        return ErrorCode.builder(s, "server error!");
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RollbackException.class)
    public ErrorCode rollbackException(RollbackException rollbackException) {
        Throwable cause = rollbackException.getCause();
        if (cause instanceof ConstraintViolationException) {
            return this.constraintViolationException((ConstraintViolationException) cause);
        }
        return ErrorCode.builder("400001", "参数校验失败");
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = TransactionSystemException.class)
    public ErrorCode transactionSystemException(TransactionSystemException transactionSystemException) {
        if (!(transactionSystemException.getCause() instanceof RollbackException)) {
            return getDefaultErrorCode(500, transactionSystemException);
        }
        return this.rollbackException((RollbackException) transactionSystemException.getCause());
    }

}
