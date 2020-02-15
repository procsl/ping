package cn.procsl.ping.business.exception;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用的异常
 * 一般是参数校验错误,请求方式错误等等
 *
 * @author procsl
 * @date 2019/12/11
 */
@Slf4j
public class CommonException extends BusinessException {

    @Setter
    protected Integer httpStatus;

    @Setter
    protected String description;

    @Override
    public Integer httpStatus() {
        return httpStatus;
    }

    @Override
    public String description() {
        return description;
    }
}
