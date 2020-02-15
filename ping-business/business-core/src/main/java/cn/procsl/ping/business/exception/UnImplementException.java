package cn.procsl.ping.business.exception;

/**
 * @author procsl
 * @date 2019/12/11
 */
public class UnImplementException extends BusinessException {

    @Override
    public Integer httpStatus() {
        return 500;
    }

    @Override
    public String description() {
        return "该功能暂未实现";
    }

    @Override
    public String getMessage() {
        return "该功能暂未实现";
    }

    @Override
    public String getCode() {
        return "001";
    }
}
