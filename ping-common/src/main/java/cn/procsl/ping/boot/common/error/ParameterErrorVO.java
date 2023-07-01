package cn.procsl.ping.boot.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import javax.sql.rowset.serial.SerialArray;
import java.io.Serializable;
import java.util.*;

@Schema(description = "格式化输出参数校验错误信息")
public class ParameterErrorVO extends ErrorVO {

    @Getter
    @Schema(description = "参数校验信息")
    final List<Error> errors = new ArrayList<>(2);


    @Builder
    public ParameterErrorVO(String code, String message) {
        super(humpToUnderline(code), message);
    }

    public void putErrorTips(String field, String tips) {
        Error error = new Error(field, tips);
        this.errors.add(error);
    }

    @Override
    @Schema(example = "METHOD_ARGUMENT_NOT_VALID_ERROR")
    public String getCode() {
        return super.getCode();
    }

    @Override
    @Schema(example = "参数校验失败")
    public String getMessage() {
        return super.getMessage();
    }


    @AllArgsConstructor
    @Getter
    public static class Error implements Serializable {
        @Schema(description = "对应错误的字段名称", example = "field")
        String field;
        @Schema(description = "对应的错误提示信息", example = "不可为空")
        String tips;
    }

}
