package cn.procsl.ping.web.component.exception;


/**
 * @author procsl
 * @date 2019/12/27
 */
public class NotFoundException extends RuntimeException{

    public NotFoundException(String method, String uri) {
        super(method+uri, null, false,false);
    }
}
