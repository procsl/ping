package cn.procsl.business.user.web.error;

import cn.procsl.business.user.web.component.SkipFilter;
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
}
