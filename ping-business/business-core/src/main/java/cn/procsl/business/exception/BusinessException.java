package cn.procsl.business.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * 业务异常
 *
 * @author procsl
 * @date 2019/12/11
 */
@Slf4j
public class BusinessException extends RuntimeException implements Serializable {

    /**
     * 业务错误代码
     * 三位
     */
    @Getter
    protected String code;

    /**
     * 获取错误码前缀
     * 该前缀为HTTP状态码
     *
     * @return HTTP status 400 - 5xx
     */
    public Integer httpStatus() {
        return 0;
    }

    /**
     * 该错误的描述
     * 可以在实例化时指定
     *
     * @return 错误描述
     */
    public String description() {
        return null;
    }

    public BusinessException() {
        super(null, null, log.isDebugEnabled(), log.isDebugEnabled());
    }

    public BusinessException(String message) {
        super(message, null, log.isDebugEnabled(), log.isDebugEnabled());
    }

    public BusinessException(String message, String code) {
        super(message, null, log.isDebugEnabled(), log.isDebugEnabled());
        this.code = code;
    }

    @Override
    @Deprecated
    public void printStackTrace() {
        log.debug("{}{}:[{}]\ndescription:\n\t{}\n", this.httpStatus(), this.getCode(), this.getMessage(), this.description());
        if (!log.isDebugEnabled()) {
            log.warn("请勿使用[printStackTrace]打印异常信息");
        }
    }

    @Override
    @Deprecated
    public void printStackTrace(PrintStream s) {
        this.printStackTrace();
    }

    @Override
    @Deprecated
    public void printStackTrace(PrintWriter s) {
        this.printStackTrace();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (log.isDebugEnabled()) {
            return super.fillInStackTrace();
        }
        return null;
    }


}
