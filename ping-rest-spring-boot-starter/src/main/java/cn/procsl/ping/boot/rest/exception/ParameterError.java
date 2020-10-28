package cn.procsl.ping.boot.rest.exception;

import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Data
public class ParameterError extends ExceptionCode {

    @Getter
    final List<Map<String, String>> errors;

    public ParameterError(String code, String message, List<Map<String, String>> errors) {
        this.setCode(code);
        this.setMessage(message);
        this.errors = errors;
    }
}
