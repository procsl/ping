package cn.procsl.ping.boot.rest.exception;

/**
 * @author procsl
 * @date 2020/03/07
 */
public class RestHttpMediaTypeNotAcceptableException extends RuntimeException{

    public RestHttpMediaTypeNotAcceptableException(String message) {
        super(message, null, false, false);
    }
}
