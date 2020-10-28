package cn.procsl.ping.boot.rest.hook;

import cn.procsl.ping.boot.rest.annotation.ApiVersion;
import cn.procsl.ping.boot.rest.annotation.Hypermedia;
import cn.procsl.ping.boot.rest.annotation.NotApiVersion;
import cn.procsl.ping.boot.rest.annotation.Ok;
import cn.procsl.ping.boot.rest.config.RestWebProperties;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

/**
 * @author procsl
 * @date 2020/03/11
 */
public class RequestMappingBuilderHook implements RegisterMappingHook {

    private final RestWebProperties properties;

    private final String[] text = new String[]{MediaType.TEXT_HTML_VALUE, MediaType.TEXT_PLAIN_VALUE};

    private final String[] consumer;

    private final String[] product;

    public RequestMappingBuilderHook(RestWebProperties properties, Map<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes) {
        this.properties = properties;
        LinkedList<String> link = new LinkedList<>();
        if (mediaTypes.containsKey(json)) {
            link.push(APPLICATION_JSON_VALUE);
        }

        if (mediaTypes.containsKey(xml)) {
            link.push(APPLICATION_XML_VALUE);
        }

        if (mediaTypes.containsKey(yaml)) {
            link.push("application/yaml");
        }
        this.consumer = link.toArray(new String[0]);

        if (properties.getRepresentationStrategy() == RestWebProperties.RepresentationStrategy.system_mime) {
            this.product = consumer;
        } else {
            link.clear();
            mediaTypes.forEach((k, v) -> link.addAll(v.stream().map(MimeType::toString).collect(Collectors.toList())));
            this.product = link.toArray(new String[0]);
        }
    }


    @Override
    public String[] paths(RequestMapping mapping, Class<?> clazz, Method method, boolean isClass, String[] paths) {
        if (!isClass) {
            return paths;
        }

        if (!properties.isEnableVersion()) {
            return paths;
        }

        Integer version = this.findVersion(method);
        if (version == null) {
            return paths;
        }
        return this.appendPathVersion(version, paths);
    }

    @Override
    public String[] products(RequestMapping requestMapping, Class<?> clazz, Method method, boolean isClass, String[] products) {

        Ok ok = AnnotatedElementUtils.getMergedAnnotation(method, Ok.class);
        if (ok == null || ok.status().equals(HttpStatus.NO_CONTENT)) {
            return new String[]{};
        }

        boolean hypermedia = AnnotatedElementUtils.hasAnnotation(method, Hypermedia.class);
        if (hypermedia) {
            return this.product;
        }

        if (!ObjectUtils.isEmpty(products)) {
            return products;
        }

        Class<?> returnType = method.getReturnType();
        boolean isPrimitive = ClassUtils.isPrimitiveOrWrapper(returnType) || CharSequence.class.isAssignableFrom(returnType);
        if (isPrimitive) {
            return text;
        }

        return this.product;
    }

    protected Integer findVersion(Method method) {

        // 如果method上或者handler上存在 noVersion 说明不加入版本管理, 将版本管理移除
        if (AnnotatedElementUtils.hasAnnotation(method, NotApiVersion.class)) {
            return null;
        }

        ApiVersion apiVersionAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, ApiVersion.class);
        int version = 1;
        if (apiVersionAnnotation != null) {
            version = apiVersionAnnotation.value();
        }
        return version;
    }

    protected String[] appendPathVersion(Integer apiVersion, String... paths) {
        if (paths.length == 0) {
            return new String[]{createPath(null, apiVersion)};
        }

        return Arrays.stream(paths).map(path -> createPath(path, apiVersion)).toArray(String[]::new);
    }

    protected String createPath(String path, Integer apiVersion) {

        String version = "v" + apiVersion;
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "/" + version;
        }

        if (path.startsWith("/")) {
            return "/" + version + path;
        }

        return "/" + version + "/" + path;
    }

}
