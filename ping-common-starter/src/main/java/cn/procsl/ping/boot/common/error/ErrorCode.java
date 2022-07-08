package cn.procsl.ping.boot.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "错误信息描述")
public class ErrorCode implements Serializable {

    @Schema(description = "错误信息编码", example = "E001")
    private String code;

    @Schema(description = "错误信息", example = "系统内部错误")
    private String message;


    public static ErrorCode builder(String code, String message) {
        return new ErrorCode(code, message);
    }

}
