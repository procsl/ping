package cn.procsl.ping.boot.common.error;

import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Data
public class ParameterErrorCode extends ErrorCode {

    @Getter
    final List<Map<String, String>> errors;

    public ParameterErrorCode(String code, String message, List<Map<String, String>> errors) {
        this.setCode(code);
        this.setMessage(message);
        this.errors = errors;
    }
}
