package cn.procsl.ping.boot.data.business.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author procsl
 * @date 2020/05/15
 */
@Slf4j
public class BusinessException extends RuntimeException {

    public BusinessException(String format, Object... arguments) {
        // TODO 这里应该用占位符的, 先写着
        super(format);
        log.debug(format, arguments);
    }
}
