package cn.procsl.ping.admin.error;

import io.swagger.v3.oas.annotations.media.Schema;
import liquibase.pro.packaged.S;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "格式化输出参数校验错误信息")
public class ParameterErrorCode extends ErrorCode {

    @Getter
    @Schema(description = "参数校验信息", example = "{\"field:\":\"不可为空\"}")
    final List<Map<String, String>> errors;


    @Builder
    public ParameterErrorCode(String code, String message, List<Map<String, String>> errors) {
        this.setCode(code);
        this.setMessage(message);
        this.errors = errors;
    }

    @Override
    @Schema(example = "E002")
    public String getCode() {
        return super.getCode();
    }

    @Override
    @Schema(example = "参数校验失败")
    public String getMessage() {
        return super.getMessage();
    }
}
