package cn.procsl.ping.admin.exception;

import lombok.*;

import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/02/19
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionCode implements Serializable {

    private String code;

    private String message;


    public static ExceptionCode builder(String code, String message) {
        return new ExceptionCode(code, message);
    }

}
