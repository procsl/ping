package cn.procsl.ping.boot.rest.web;


/**
 * @author procsl
 * @date 2019/12/27
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("Not Found", null, false, false);
    }
}
