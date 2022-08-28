package cn.procsl.ping.boot.common.error;

import cn.procsl.ping.boot.common.dto.MessageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/02/19
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Schema(description = "错误信息描述")
public class ErrorCode extends MessageDTO implements Serializable, ErrorEntity {

    @Schema(description = "错误信息编码", example = "001")
    private String code;


    public ErrorCode(String message) {
        super(message);
    }

    public ErrorCode() {
        super("系统内部错误");
        this.code = "001";
    }

    public ErrorCode(String code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    @Schema(description = "错误信息", example = "系统内部错误")
    public String getMessage() {
        return super.getMessage();
    }

    public static ErrorCode builder(String code, String message) {
        return new ErrorCode(code, message);
    }

}
