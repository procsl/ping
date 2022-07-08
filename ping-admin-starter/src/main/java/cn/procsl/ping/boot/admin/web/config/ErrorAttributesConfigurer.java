package cn.procsl.ping.boot.admin.web.config;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorAttributesConfigurer implements ErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        return Map.of("code", "4000001", "messages", "出现了一个错误", "trace", "消息");
    }

    @Override
    public Throwable getError(WebRequest webRequest) {
        return null;
    }
}
