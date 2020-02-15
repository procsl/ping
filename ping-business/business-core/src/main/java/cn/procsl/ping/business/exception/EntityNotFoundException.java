package cn.procsl.ping.business.exception;

/**
 * @author procsl
 * @date 2019/12/11
 */
public class EntityNotFoundException extends BusinessException {

    @Override
    public Integer httpStatus() {
        return 403;
    }

}
