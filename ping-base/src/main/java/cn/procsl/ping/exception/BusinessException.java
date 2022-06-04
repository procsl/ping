package cn.procsl.ping.exception;

import cn.procsl.ping.web.ErrorEntity;
import lombok.NonNull;

/**
 * 业务异常
 *
 * @author procsl
 * @date 2020/05/15
 */
public class BusinessException extends RuntimeException implements ErrorEntity {

    final static String MESSAGE_TEMPLATE = "[%s] not found!";

    Integer httpStatus = 501;

    String code = "000";

    public BusinessException(String format, Object... arguments) {
        // TODO 这里应该用占位符的, 先写着
        super(format, null, true, false);
    }

    public static <T> T ifNotFound(T entity, @NonNull String message) {
        if (entity == null) {
            throw new IllegalArgumentException(String.format(MESSAGE_TEMPLATE, message));
        }
        return entity;
    }

    public static <T> void ifNotFoundOnlyThrow(T entity, @NonNull String message) {
        if (entity == null) {
            throw new IllegalArgumentException(String.format(MESSAGE_TEMPLATE, message));
        }
    }

    @Override
    public Integer httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return super.getMessage();
    }
}
