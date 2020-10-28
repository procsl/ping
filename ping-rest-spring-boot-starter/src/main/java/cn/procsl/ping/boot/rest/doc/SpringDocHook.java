package cn.procsl.ping.boot.rest.doc;

import cn.procsl.ping.boot.rest.annotation.ExceptionHandler;
import cn.procsl.ping.boot.rest.annotation.Ok;
import cn.procsl.ping.boot.rest.hook.RegisterMappingHook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
public class SpringDocHook implements RegisterMappingHook {

    /**
     * 处理完成时
     *
     * @param method      目标方法
     * @param handlerType 目标类型
     * @param info        映射方法
     */
    @Override
    @SneakyThrows
    public void onComplete(Method method, Class<?> handlerType, RequestMappingInfo info) {
        Operation opera = method.getAnnotation(Operation.class);
        if (opera != null) {
            return;
        }

        Field field = ReflectionUtils.findField(method.getClass(), "declaredAnnotations");
        if (field == null) {
            return;
        }

        field.setAccessible(true);
        Object tmp = field.get(method);
        field.setAccessible(false);
        if (!(tmp instanceof LinkedHashMap)) {
            return;
        }
        LinkedHashMap<Class<? extends Annotation>, Annotation> map = (LinkedHashMap<Class<? extends Annotation>, Annotation>) tmp;

        Map<String, Object> maps = createOperation(method, info);
        opera = AnnotationUtils.synthesizeAnnotation(maps, Operation.class, method);
        map.put(Operation.class, opera);
    }

    private Map<String, Object> createOperation(Method method, RequestMappingInfo info) {

        Ok success = AnnotatedElementUtils.findMergedAnnotation(method, Ok.class);
        success = success == null ? AnnotationUtils.synthesizeAnnotation(Ok.class) : success;

        Map<String, Object> apiMaps = new HashMap<>(15);
        apiMaps.put("description", success.message());
        apiMaps.put("responseCode", String.valueOf(success.status().value()));
        apiMaps.put("content", this.createContentType(method, info));
        ApiResponse create = AnnotationUtils.synthesizeAnnotation(apiMaps, ApiResponse.class, null);

        Set<ExceptionHandler> errorHandlers = AnnotatedElementUtils.findMergedRepeatableAnnotations(method, ExceptionHandler.class);
        List<ApiResponse> responses = errorHandlers.stream().map(item -> {
            Map<String, Object> tmp = new HashMap<>(2);
            tmp.put("description", item.message());
            tmp.put("responseCode", item.status().value() + item.code());
            return AnnotationUtils.synthesizeAnnotation(tmp, ApiResponse.class, null);
        }).collect(Collectors.toList());

        responses.add(create);

        HashMap<String, Object> map = new HashMap<>(1);
        map.put("responses", responses.toArray(new ApiResponse[0]));
        return map;
    }

    public Content[] createContentType(Method method, RequestMappingInfo info) {
        Schema schema = createSchema(method.getReturnType());
        Set<MediaType> stream = info.getProducesCondition().getProducibleMediaTypes();
        return stream.stream()
            .map(item -> {
                Map<String, Object> tmp = new HashMap<>(2);
                tmp.put("mediaType", item.toString());
                tmp.put("schema", schema);
                Content content = AnnotationUtils.synthesizeAnnotation(tmp, Content.class, null);
                return content;
            }).toArray(value -> new Content[stream.size()]);
    }

    public Schema createSchema(Class<?> clazz) {
        Map<String, Object> tmp = new HashMap<>(1);
        tmp.put("implementation", clazz);
        Schema schema = AnnotationUtils.synthesizeAnnotation(tmp, Schema.class, null);
        return schema;
    }

}
