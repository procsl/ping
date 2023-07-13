package cn.procsl.ping.boot.web.component;

import cn.procsl.ping.boot.common.dto.MessageVO;
import cn.procsl.ping.boot.common.error.ErrorVO;
import cn.procsl.ping.boot.common.error.ParameterErrorVO;
import cn.procsl.ping.boot.web.cipher.CipherGenericConverter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Indexed;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;
import java.util.List;

@Indexed
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageVO exceptionHandler(NullPointerException e) {
        log.error("服务器内部错误", e);
        return new MessageVO("服务器内部错误");
    }

//
//    @ResponseBody
//    @ExceptionHandler(value = Exception.class)
//    public MessageVO exceptionHandler(Exception e, HandlerMethod handlerMethod) {
//        log.warn("未处理的异常:", e);
//        return new MessageVO("服务器内部错误");
//    }

//    @ResponseBody
//    @ExceptionHandler(value = RuntimeException.class)
//    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
//    public MessageVO runtimeExceptionHandler(RuntimeException e) {
//        log.warn("未处理的异常:", e);
//        return new MessageVO("服务器内部错误");
//    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ParameterErrorVO methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ParameterErrorVO errorVo = new ParameterErrorVO("MethodArgumentTypeMismatchException", "参数解析失败");
        Object value = e.getValue();
        if (value instanceof String || value instanceof Number || value instanceof Date) {
            errorVo.putErrorTips(e.getName(), value.toString());
        } else {
            log.warn("参数解析错误: ", e);
            errorVo.putErrorTips(e.getName(), "参数错误");
        }
        return errorVo;
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ParameterErrorVO methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        List<ObjectError> errors = result.getAllErrors();
        if (CollectionUtils.isEmpty(errors)) {
            log.warn("处理参数校验失败异常时, 找不到参数校验信息", e);
        }

        ParameterErrorVO errorVo = new ParameterErrorVO("MethodArgumentNotValid", "参数校验失败");

        errors.forEach(error -> {
            if (error instanceof FieldError) {
                errorVo.putErrorTips(((FieldError) error).getField(), error.getDefaultMessage());
            } else {
                errorVo.putErrorTips(error.getObjectName(), error.getDefaultMessage());
            }
        });
        return errorVo;
    }


    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ParameterErrorVO constraintViolationException(ConstraintViolationException constraintViolationException) {

        ParameterErrorVO errorVo = new ParameterErrorVO("MethodArgumentNotValid", "参数校验失败");
        for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
            String key = violation.getPropertyPath().toString();
            errorVo.putErrorTips(key, violation.getMessage());
        }
        return errorVo;
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ErrorVO httpMessageNotReadableException() {
        return ErrorVO.build(HttpMessageNotReadableException.class, "请求数据解析失败");
    }


    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ErrorVO httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return ErrorVO.build(exception, String.format("不支持[%s]请求方式", exception.getMethod()));
    }

    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = CipherGenericConverter.ConverterException.class)
    public ParameterErrorVO decryptException(CipherGenericConverter.ConverterException exception) {
        ParameterErrorVO tmp = new ParameterErrorVO("MethodArgumentNotValid", "请求数据解析失败");
        tmp.putErrorTips(exception.getFiledName(), exception.getSource());
        return tmp;
    }

}
