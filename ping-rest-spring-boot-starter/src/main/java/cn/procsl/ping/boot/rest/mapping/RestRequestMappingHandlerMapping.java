package cn.procsl.ping.boot.rest.mapping;

import cn.procsl.ping.boot.rest.hook.RegisterMappingHook;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author procsl
 * @date 2020/02/21
 */
@Slf4j
public class RestRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    final List<RegisterMappingHook> registerHook;

    public RestRequestMappingHandlerMapping(@NonNull List<RegisterMappingHook> registerHook) {
        this.registerHook = new ArrayList<>(registerHook);
        registerHook.sort(Comparator.comparingInt(RegisterMappingHook::getOrder));
    }

    public static RequestMappingInfo.BuilderConfiguration findConfig(RequestMappingHandlerMapping
                                                                         requestMappingHandlerMapping) {
        Field configField = ReflectionUtils.findField(requestMappingHandlerMapping.getClass(), "config", RequestMappingInfo.BuilderConfiguration.class);
        assert configField != null;
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
        assert method != null;
        method.setAccessible(true);
        Object obj = ReflectionUtils.invokeMethod(method, instance, handlerType);
        method.setAccessible(false);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        registerHook.forEach(i -> i.init(this));
    }

//    @Override
//    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
//
//
//        boolean bool = (AnnotatedElementUtils.hasAnnotation(handlerType, RestEndpoint.class) || AnnotatedElementUtils.hasAnnotation(method, RestEndpoint.class)) && (!CollectionUtils.isEmpty(registerHook));
//
//        if (!bool) {
//            return super.getMappingForMethod(method, handlerType);
//        }
//
//        registerHook.forEach(i -> i.onBuild(method, handlerType));
//
//        RequestMappingInfo.BuilderConfiguration config = findConfig(this);
//        assert config != null;
//
//        RequestMappingInfo info = createRequestMappingInfo(handlerType, method, false, config);
//        if (info == null) {
//            return null;
//        }
//
//        RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType, method, true, config);
//        if (typeInfo != null) {
//            info = typeInfo.combine(info);
//        }
//
//        String prefix = callSuperGetPathPrefix(this, handlerType);
//        if (prefix != null) {
//            info = RequestMappingInfo.paths(prefix).options(config).build().combine(info);
//        }
//
//        final RequestMappingInfo tmp = info;
//        registerHook.forEach(i -> i.onComplete(method, handlerType, tmp));
//        log.trace("创建映射:{}", info);
//        return info;
//    }

    protected RequestMappingInfo createRequestMappingInfo(Class clazz,
                                                          Method method,
                                                          boolean isClass,
                                                          RequestMappingInfo.BuilderConfiguration config
    ) {
        @NonNull
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(isClass ? clazz : method, RequestMapping.class);

        RequestCondition<?> condition;
        if (isClass) {
            condition = this.getCustomTypeCondition(clazz);
        } else {
            condition = this.getCustomMethodCondition(method);
        }

        assert requestMapping != null;
        String[] paths = requestMapping.path();
        RequestMethod[] methods = requestMapping.method();
        String[] params = requestMapping.params();
        String[] headers = requestMapping.headers();
        String[] consumer = requestMapping.consumes();
        String[] products = requestMapping.produces();

        RequestMappingInfo.Builder builder;
        for (RegisterMappingHook hook : registerHook) {
            paths = hook.paths(requestMapping, clazz, method, isClass, paths);
            methods = hook.methods(requestMapping, clazz, method, isClass, methods);
            params = hook.params(requestMapping, clazz, method, isClass, params);
            headers = hook.headers(requestMapping, clazz, method, isClass, headers);
            consumer = hook.consumer(requestMapping, clazz, method, isClass, consumer);
            products = hook.products(requestMapping, clazz, method, isClass, products);
        }

        builder = RequestMappingInfo
            .paths(resolveEmbeddedValuesInPatterns(paths))
            .methods(methods)
            .params(params)
            .headers(headers)
            .consumes(consumer)
            .produces(products)
            .mappingName(requestMapping.name());

        if (condition != null) {
            builder.customCondition(condition);
        }
        return builder.options(config).build();

    }

}
