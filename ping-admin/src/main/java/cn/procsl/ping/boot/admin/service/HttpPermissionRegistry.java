package cn.procsl.ping.boot.admin.service;

import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.ArrayList;
import java.util.Map;

@Indexed
@Component
@Slf4j
@RequiredArgsConstructor
public class HttpPermissionRegistry {

    final WebApplicationContext webApplicationContext;

    public ArrayList<Permission> httpPermissionScanner() {
        ArrayList<Permission> list = new ArrayList<>();
        Map<String, RequestMappingHandlerMapping> mappings = webApplicationContext.getBeansOfType(
                RequestMappingHandlerMapping.class);
        mappings.forEach((name, mapping) -> mapping.getHandlerMethods().forEach((k, v) -> {
            for (RequestMethod method : k.getMethodsCondition().getMethods()) {
                assert k.getPathPatternsCondition() != null;
                for (PathPattern path : k.getPathPatternsCondition().getPatterns()) {
                    Permission httpPermission = HttpPermission.create(method.name(), path.getPatternString());
                    Operation operation = v.getMethodAnnotation(Operation.class);
                    if (operation != null) {
                        httpPermission.setSummary(operation.summary());
                        httpPermission.setDescription(operation.description());
                    }
                    list.add(httpPermission);
                }
            }
        }));
        return list;
    }

}
