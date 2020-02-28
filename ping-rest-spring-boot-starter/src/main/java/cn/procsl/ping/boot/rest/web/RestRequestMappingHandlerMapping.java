package cn.procsl.ping.boot.rest.web;

import cn.procsl.ping.boot.rest.annotation.ApiVersion;
import cn.procsl.ping.boot.rest.annotation.NotApiVersion;
import cn.procsl.ping.boot.rest.config.RestWebProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cn.procsl.ping.boot.rest.utils.MediaTypeUtils.createMediaType;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

/**
 * @author procsl
 * @date 2020/02/21
 */
@Slf4j
public class RestRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private String[] MEDIA_TYPES = null;

    final RestWebProperties properties;

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {

        for (String s : properties.getRestControllerPackageName()) {
            String clazz = method.getDeclaringClass().getName();
            if (clazz.startsWith(s)) {
                return createRequestMappingInfo(method, handlerType);
            }
        }

        return super.getMappingForMethod(method, handlerType);
    }

    protected RequestMappingInfo createRequestMappingInfo(Method method, Class<?> handlerType) {
        RequestMappingInfo.BuilderConfiguration config = findConfig(this);

        Integer version = getVersion(method, handlerType);
        RequestMappingInfo info = createRequestMappingInfo(method, version, config);
        if (info == null) {
            return null;
        }

        RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType, version, config);
        if (typeInfo != null) {
            info = typeInfo.combine(info);
        }

        String prefix = callSuperGetPathPrefix(this, handlerType);
        if (prefix != null) {
            info = RequestMappingInfo.paths(prefix).options(config).build().combine(info);
        }

        log.trace("创建映射:{}", info);
        return info;
    }

    public Integer getVersion(Method method, Class<?> handlerType) {
        if (!properties.isEnableVersion()) {
            return null;
        }

        // 如果method上或者handler上存在 noVersion 说明不加入版本管理, 将版本管理移除
        NotApiVersion notApiVersion = AnnotationUtils.getAnnotation(handlerType, NotApiVersion.class);
        if (notApiVersion == null) {
            notApiVersion = AnnotationUtils.getAnnotation(method, NotApiVersion.class);
        }

        if (notApiVersion != null) {
            return null;
        }

        ApiVersion apiVersionAnnotation = AnnotationUtils.getAnnotation(method, ApiVersion.class);
        Integer version = 1;
        if (apiVersionAnnotation != null) {
            version = apiVersionAnnotation.value();
        }

        return version;
    }

    public static RequestMappingInfo.BuilderConfiguration findConfig(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        Field configField = ReflectionUtils.findField(requestMappingHandlerMapping.getClass(), "config", RequestMappingInfo.BuilderConfiguration.class);
        configField.setAccessible(true);
        Object obj = ReflectionUtils.getField(configField, requestMappingHandlerMapping);
        configField.setAccessible(false);
        if (obj != null) {
            return (RequestMappingInfo.BuilderConfiguration) obj;
        }
        return null;
    }

    public static String callSuperGetPathPrefix(RequestMappingHandlerMapping instance, Class<?> handlerType) {
        Method method = ReflectionUtils.findMethod(instance.getClass(), "getPathPrefix", Class.class);
        method.setAccessible(true);
        Object obj = ReflectionUtils.invokeMethod(method, instance, handlerType);
        method.setAccessible(false);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    protected RequestMappingInfo createRequestMappingInfo(AnnotatedElement element,
                                                          Integer apiVersion,
                                                          RequestMappingInfo.BuilderConfiguration config
    ) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);

        if (requestMapping == null) {
            return null;
        }

        RequestCondition<?> condition;
        Method method = null;
        String[] paths;
        if (element instanceof Class) {
            condition = this.getCustomTypeCondition((Class<?>) element);
            paths = buildPathByVersion(requestMapping, apiVersion);
        } else {
            condition = this.getCustomMethodCondition((Method) element);
            method = (Method) element;
            paths = buildPathByVersion(requestMapping, null);
        }

        RequestMappingInfo.Builder builder = RequestMappingInfo
                .paths(paths)
                .methods(requestMapping.method())
                .params(requestMapping.params())
                .headers(requestMapping.headers())
                .consumes(requestMapping.consumes())
                .produces(buildProductsByReturnValueType(requestMapping, method, apiVersion))
                .mappingName(requestMapping.name());
        if (condition != null) {
            builder.customCondition(condition);
        }
        return builder.options(config).build();

    }

    protected String[] buildProductsByReturnValueType(RequestMapping requestMapping, Method method, Integer version) {
        String[] products = requestMapping.produces();

        if (!ObjectUtils.isEmpty(products)) {
            return products;
        }

        if (StringUtils.isEmpty(properties.getMimeSubtype())) {
            return products;
        }

        // 如果当前不为方法,则直接返回默认的
        if (method == null) {
            return products;
        }

        // 添加自定义的 类型
        if (ObjectUtils.isEmpty(version)) {
            return MEDIA_TYPES;
        }

        return new String[]{
                createMediaType(properties.getMimeSubtype() + ".v" + version + "+json").toString(),
                createMediaType(properties.getMimeSubtype() + ".v" + version + "+xml").toString(),
                APPLICATION_JSON_VALUE,
                APPLICATION_XML_VALUE
        };
    }

    protected String[] buildPathByVersion(RequestMapping requestMapping, Integer apiVersion) {
        // 创建版本
        String[] paths = appendPathVersion(apiVersion, requestMapping.path());
        // 解析占位符
        return resolveEmbeddedValuesInPatterns(paths);
    }

    public static String[] appendPathVersion(Integer apiVersion, String... paths) {
        if (paths.length == 0) {
            return new String[]{createPath(null, apiVersion)};
        }

        List<String> result = Arrays.stream(paths).map(path -> createPath(path, apiVersion)).collect(Collectors.toList());
        return result.toArray(new String[result.size()]);
    }

    public static List<String> create(List<String> list, String type) {
        return list.stream().map(path -> path.endsWith(type) ? path : path + type).collect(Collectors.toList());
    }

    public static String createPath(String path, Integer apiVersion) {
        if (apiVersion == null) {
            return path;
        }
        String version = "v" + apiVersion;
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "/" + version;
        }
        if (path.startsWith("/")) {
            return "/" + version + path;
        } else {
            return "/" + version + "/" + path;
        }
    }


    public RestRequestMappingHandlerMapping(RestWebProperties properties) {
        this.properties = properties;
        if (ObjectUtils.isEmpty(properties.getMimeSubtype())) {
            return;
        }
        MEDIA_TYPES = new String[]{
                createMediaType(properties.getMimeSubtype() + "+json").toString(),
                createMediaType(properties.getMimeSubtype() + "+xml").toString(),
                APPLICATION_JSON_VALUE,
                APPLICATION_XML_VALUE
        };
    }
}
