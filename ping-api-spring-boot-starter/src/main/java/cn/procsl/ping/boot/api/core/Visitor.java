package cn.procsl.ping.boot.api.core;

import io.swagger.v3.oas.models.PathItem;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * @author procsl
 * @date 2020/02/26
 */
public interface Visitor {
    /**
     * 根据相关的上下文创建item信息
     *
     * @param item
     * @param requestMappingInfo
     * @param handlerMethod
     */
    void full(PathItem item, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod);
}
