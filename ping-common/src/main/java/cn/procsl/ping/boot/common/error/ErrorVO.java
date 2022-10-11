package cn.procsl.ping.boot.common.error;

import cn.procsl.ping.boot.common.dto.MessageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;

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
public class ErrorVO extends MessageVO implements Serializable, ErrorEntity {

    @Schema(description = "错误信息编码", example = "001")
    @NonNull
    private String code;

    public ErrorVO(String message) {
        super(message);
    }

    public ErrorVO() {
        super("系统内部错误");
        this.code = "001";
    }

    public ErrorVO(String code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    @Schema(description = "错误信息", example = "系统内部错误")
    public String getMessage() {
        return super.getMessage();
    }

    public static ErrorVO builder(String code, String message) {
        return new ErrorVO(code, message);
    }

}
