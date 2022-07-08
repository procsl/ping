package cn.procsl.ping.boot.common.error;

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
public class ErrorCode implements Serializable {

    private String code;

    private String message;


    public static ErrorCode builder(String code, String message) {
        return new ErrorCode(code, message);
    }

}
