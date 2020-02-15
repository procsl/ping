package cn.procsl.ping.business.exception;

/**
 * 实体已存在异常
 *
 * @author procsl
 * @date 2019/12/11
 */
public class EntityExistException extends BusinessException {

    @Override
    public Integer httpStatus() {
        return 403;
    }

}
