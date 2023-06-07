package cn.procsl.ping.boot.common.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;


@Slf4j
public class CommonErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

        Throwable error = this.getError(webRequest);
        if (error instanceof BusinessException) {
            return ErrorVO.build((BusinessException) error).convertToMap();
        }

        Map<String, Object> attr = super.getErrorAttributes(webRequest, options);
        if (error == null && attr.get("status").equals(404)) {
            Object message = attr.get("error");
            return ErrorVO.build("RESOURCE_NOT_FOUND", (String) message).convertToMap();
        }
        // TODO
        log.warn("未处理错误异常", error);
        return attr;
    }
}
