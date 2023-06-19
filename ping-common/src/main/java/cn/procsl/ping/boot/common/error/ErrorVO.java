package cn.procsl.ping.boot.common.error;

import cn.procsl.ping.boot.common.dto.MessageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author procsl
 * @date 2020/02/19
 */
@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Schema(description = "错误信息描述")
public class ErrorVO extends MessageVO implements Serializable, ErrorEntity {

    @Schema(description = "错误信息编码", example = "PERMISSION_DENIED")
    private String code;

    public static ErrorVO build(@NonNull Exception e) {
        String name = e.getClass().getSimpleName();
        String code = humpToUnderline(name);
        return new ErrorVO(code, e.getMessage());
    }

    public static ErrorVO build(@NonNull Exception e, String message) {
        String name = e.getClass().getSimpleName();
        String code = humpToUnderline(name);
        return new ErrorVO(code, message);
    }

    public static ErrorVO build(@NonNull String e, String message) {
        return new ErrorVO(e, message);
    }

    public static <A extends Exception> ErrorVO build(@NonNull Class<A> e, String message) {
        String name = e.getName();
        String code = humpToUnderline(name);
        return new ErrorVO(code, message);
    }

    static String humpToUnderline(String str) {
        String regex = "([A-Z])";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while (matcher.find()) {
            String target = matcher.group();
            str = str.replaceAll(target, "_" + target.toLowerCase());
        }
        str = str.replaceAll("^_", "");
        return str.toUpperCase();
    }

    public ErrorVO(String code, String message) {
        super(message);
        code = code.replaceAll("_EXCEPTION$", "_ERROR");
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

    public Map<String, Object> convertToMap() {
        return Map.of("code", this.code, "message", this.getMessage());
    }
}
