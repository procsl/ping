package cn.procsl.ping.boot.rest.exception;


/**
 * @author procsl
 * @date 2019/12/27
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String method, String uri) {
        super(String.format("[%s %s] Not Found", method, uri), null, false, false);
    }
}
