package cn.procsl.ping.boot.rest.hook;

import cn.procsl.ping.boot.rest.annotation.ApiVersion;
import cn.procsl.ping.boot.rest.annotation.NotApiVersion;
import cn.procsl.ping.boot.rest.config.RestWebProperties;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.*;
import static java.util.Arrays.binarySearch;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

/**
 * @author procsl
 * @date 2020/03/11
 */
public class RequestMappingBuilderHook implements RegisterMappingHook {

    private final RestWebProperties properties;

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
        this.consumer = link.toArray(new String[link.size()]);

        if (properties.getRepresentationStrategy() == RestWebProperties.RepresentationStrategy.system_mime) {
            this.product = consumer;
        } else {
            link.clear();
            mediaTypes.forEach((k, v) -> link.addAll(v.stream().map(item -> item.toString()).collect(Collectors.toList())));
            this.product = link.toArray(new String[link.size()]);
        }
    }

    /**
     * 构建路径
     *
     * @param mapping
     * @param clazz
     * @param method
     * @param isClass
     * @param paths
     * @return
     */
    @Override
    public String[] paths(RequestMapping mapping, Class clazz, Method method, boolean isClass, String[] paths) {
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
    public String[] consumer(RequestMapping requestMapping, Class clazz, Method method, boolean isClass, String[] consumer) {
        if (!ObjectUtils.isEmpty(consumer)) {
            return consumer;
        }

        boolean isApi = AnnotatedElementUtils.hasAnnotation(method, ResponseBody.class) || AnnotatedElementUtils.hasAnnotation(clazz, ResponseBody.class);
        if (!isApi) {
            return consumer;
        }

        RequestMethod[] methods = requestMapping.method();
        // TODO 这里有问题, 傻逼了
        boolean bool =
                methods == null
                        ||
                        methods.length == 0
                        ||
                        binarySearch(methods, RequestMethod.GET) >= 0
                        ||
                        binarySearch(methods, RequestMethod.DELETE) >= 0
                        ||
                        binarySearch(methods, RequestMethod.OPTIONS) >= 0
                        ||
                        binarySearch(methods, RequestMethod.TRACE) >= 0
                        ||
                        binarySearch(methods, RequestMethod.HEAD) >= 0;

        // 没有请求体的方法 不设置
        if (bool) {
            return consumer;
        }

        return this.consumer;
    }

    @Override
    public String[] products(RequestMapping requestMapping, Class clazz, Method method, boolean isClass, String[] products) {
        if (!ObjectUtils.isEmpty(products)) {
            return products;
        } boolean isApi = AnnotatedElementUtils.hasAnnotation(method, ResponseBody.class) ||


                AnnotatedElementUtils.hasAnnotation(clazz, ResponseBody.class);
        if (!isApi) {
            return products;
        }

        return this.product;
    }

    protected Integer findVersion(Method method) {

        // 如果method上或者handler上存在 noVersion 说明不加入版本管理, 将版本管理移除
        if (AnnotatedElementUtils.hasAnnotation(method, NotApiVersion.class)) {
            return null;
        }

        ApiVersion apiVersionAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, ApiVersion.class);
        Integer version = 1;
        if (apiVersionAnnotation != null) {
            version = apiVersionAnnotation.value();
        }
        return version;
    }

    protected String[] appendPathVersion(Integer apiVersion, String... paths) {
        if (paths.length == 0) {
            return new String[]{createPath(null, apiVersion)};
        }

        List<String> result = Arrays.stream(paths).map(path -> createPath(path, apiVersion)).collect(Collectors.toList());
        return result.toArray(new String[result.size()]);
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
