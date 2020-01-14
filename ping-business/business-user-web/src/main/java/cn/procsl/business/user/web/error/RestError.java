package cn.procsl.business.user.web.error;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author procsl
 * @date 2019/12/25
 */
@Data
@Accessors(chain = true)
public class RestError {

    private String code;

    private String message;
}
