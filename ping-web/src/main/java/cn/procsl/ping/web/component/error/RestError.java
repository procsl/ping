package cn.procsl.ping.web.component.error;

import cn.procsl.ping.web.serializable.SkipFilter;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author procsl
 * @date 2019/12/25
 */
@Data
@Accessors(chain = true)
@SkipFilter
public class RestError {

    private String code;

    private String message;

    public static RestError create(String code, String message) {
        return new RestError().setCode(code).setMessage(message);
    }
}
