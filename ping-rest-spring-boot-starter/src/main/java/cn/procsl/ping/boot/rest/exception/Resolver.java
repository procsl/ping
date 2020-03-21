package cn.procsl.ping.boot.rest.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/03/19
 */
@Getter
@Setter
public class Resolver implements Serializable {
    private HttpStatus status;
    private Integer code;
    private String message;

    public  boolean Override(){
        return true;
    }
}
