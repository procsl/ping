package cn.procsl.ping.business.exception;

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

    public BusinessException(String format, Object... arguments) {
        // TODO 这里应该用占位符的, 先写着
        super(format);
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
        return null;
    }

    @Override
    public String code() {
        return null;
    }

    @Override
    public String message() {
        return null;
    }
}
