package cn.procsl.business.user.web.components.exception;


import cn.procsl.business.exception.BusinessException;

/**
 * @author procsl
 * @date 2019/12/27
 */
public class NotFoundException extends BusinessException {

    String httpMethod;
    String requestURL;

    public NotFoundException(String httpMethod, String requestURL) {
        this.httpMethod = httpMethod;
        this.requestURL = requestURL;
        this.code = "001";
    }

    @Override
    public Integer httpStatus() {
        return 404;
    }

    @Override
    public String getMessage() {
        return "Not Found";
    }
}
