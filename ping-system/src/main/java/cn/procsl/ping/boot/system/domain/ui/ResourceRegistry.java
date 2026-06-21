package cn.procsl.ping.boot.system.domain.ui;


import lombok.AllArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ResourceRegistry {

    private final RequestMappingHandlerMapping handlerMapping;

    // 待完善, 返回 ui/*/index.json 下并解析后的文件
    // @API:name 将接口的入参,字段解析为schema返回
    // 必须要扫描所有的ui文件夹下的文件并缓存
    public List<Component> getComponentById(Long id) {
        return null;
    }


    public List<ResourceMetadata> getAllResources() {

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        ArrayList<ResourceMetadata> resourceMetadata = new ArrayList<>();
        handlerMethods.forEach((mappingInfo, handlerMethod) -> {
            Class<?> beanType = handlerMethod.getBeanType();
            Method method = handlerMethod.getMethod();

            ResourceReference annotation = null;
            if (AnnotatedElementUtils.hasAnnotation(method, ResourceReference.class)) {
                annotation = AnnotatedElementUtils.findMergedAnnotation(method, ResourceReference.class);
            } else if (AnnotatedElementUtils.hasAnnotation(beanType, ResourceReference.class)) {
                annotation = AnnotatedElementUtils.findMergedAnnotation(beanType, ResourceReference.class);
            }

            if (annotation != null) {
                ResourceRegistry.ResourceMetadata metadata = new ResourceRegistry.ResourceMetadata(annotation, mappingInfo, handlerMethod);
                // 3. 直接存入 List 容器
                resourceMetadata.add(metadata);
            }

        });

        return Collections.unmodifiableList(resourceMetadata);
    }


    @AllArgsConstructor
    public static class ResourceMetadata {
        private final ResourceReference resourceName;
        private final RequestMappingInfo mappingInfo;
        private final HandlerMethod handlerMethod;

        /**
         * 内聚成员方法：动态提取 HTTP 请求方法
         */
        public String getHttpMethod() {
            if (mappingInfo == null) return "ALL";
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
            return methods.isEmpty() ? "ALL" : methods.toString();
        }

        /**
         * 内聚成员方法：动态提取所有的 URL 路径模式
         */
        public Set<String> getPaths() {
            if (mappingInfo == null) return Collections.emptySet();

            Set<String> patterns = mappingInfo.getDirectPaths();
            // 兼容 Spring Boot 3/4 的不同 Path 匹配策略
            if (patterns.isEmpty() && mappingInfo.getPathPatternsCondition() != null) {
                patterns = mappingInfo.getPathPatternsCondition().getPatternValues();
            }
            return patterns;
        }

        /**
         * 内聚成员方法：获取控制器类名
         */
        public String getClassName() {
            return handlerMethod != null ? handlerMethod.getBeanType().getSimpleName() : "";
        }

        /**
         * 内聚成员方法：获取方法名
         */
        public String getMethodName() {
            return handlerMethod != null ? handlerMethod.getMethod().getName() : "";
        }

        /**
         * 内聚成员方法：获取注解中定义的资源名称/Tag
         */
        public String getResourceTag() {
            return resourceName.toString();
        }

        public Component toComponent() {
            Component component = new Component();
            component.setName(resourceName.name());
            component.addAttribute("paths", this.getPaths());
            component.addAttribute("method", this.getHttpMethod());
            return component;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s -> %s.%s() (Tag: %s)",
                getHttpMethod(), getPaths(), getClassName(), getMethodName(), getResourceTag());
        }

    }
}
