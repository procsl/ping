package cn.procsl.ping.boot.doc.core;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/02/25
 */
@Slf4j
@RequiredArgsConstructor
public class OpenApiCustom implements OpenApiCustomiser {

    final RestWebProperties properties;

    final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private Map<RequestMappingInfo, HandlerMethod> links;

    protected final List<Visitor> pathFunctionList;

    @Override
    public void customise(OpenAPI openApi) {
        load();
        openApi.getPaths().forEach(this::create);
    }

    protected void create(String url, PathItem item) {
        links.forEach((requestMappingInfo, handlerMethod) -> {
            for (String pattern : requestMappingInfo.getPatternsCondition().getPatterns()) {
                if (url.equals(pattern)) {
                    this.pathFunctionList.forEach(v -> {
                        v.full(item, requestMappingInfo, handlerMethod);
                    });
                }
            }
        });
    }

    public void load() {
        if (links != null) {
            return;
        }
        Field mappingRegistryField = ReflectionUtils.findField(this.requestMappingHandlerMapping.getClass(), "mappingRegistry");
        mappingRegistryField.setAccessible(true);
        Object mappingRegistry = ReflectionUtils.getField(mappingRegistryField, this.requestMappingHandlerMapping);
        mappingRegistryField.setAccessible(false);
        Field mappingLookup = ReflectionUtils.findField(mappingRegistry.getClass(), "mappingLookup", Map.class);
        mappingLookup.setAccessible(true);
        Object target = ReflectionUtils.getField(mappingLookup, mappingRegistry);
        mappingLookup.setAccessible(false);
        if (target != null) {
            links = (Map<RequestMappingInfo, HandlerMethod>) target;
        }
    }

}
