package cn.procsl.ping.boot.api.core;

import cn.procsl.ping.boot.rest.annotation.NoContent;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.*;

/**
 * @author procsl
 * @date 2020/02/26
 */
@RequiredArgsConstructor
public class RestVisitor implements Visitor {

    final String VND_XML;

    final String VND_JSON;


    @Override
    public void full(PathItem item, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {

        //当前的方法
        Set<RequestMethod> methods = requestMappingInfo.getMethodsCondition().getMethods();
        return;
//
//        for (RequestMethod method : methods) {
//            if ("GET".equals(method.name())) {
//                Operation get = item.getGet();
//                if (get != null) {
//                    this.push(item, get, method, requestMappingInfo, handlerMethod);
//                    break;
//                }
//            }
//
//            if ("POST".equals(method.name())) {
//                Operation post = item.getPost();
//                if (post != null) {
//                    this.push(item, post, method, requestMappingInfo, handlerMethod);
//                    break;
//                }
//            }
//
//
//            if ("DELETE".equals(method.name())) {
//                Operation delete = item.getDelete();
//                if (delete != null) {
//                    this.push(item, delete, method, requestMappingInfo, handlerMethod);
//                    break;
//                }
//            }
//
//            if ("PATCH".equals(method.name())) {
//                Operation patch = item.getPatch();
//                if (patch != null) {
//                    this.push(item, patch, method, requestMappingInfo, handlerMethod);
//                    break;
//                }
//            }
//
//            if ("PUT".equals(method.name())) {
//                Operation put = item.getPut();
//                if (put != null) {
//                    this.push(item, put, method, requestMappingInfo, handlerMethod);
//                    break;
//                }
//            }
//
//            if ("HEAD".equals(method.name())) {
//                Operation head = item.getHead();
//                if (head != null) {
//                    this.push(item, head, method, requestMappingInfo, handlerMethod);
//                    break;
//                }
//            }
//
//            if ("TRACE".equals(method.name())) {
//                Operation trace = item.getTrace();
//                if (trace != null) {
//                    this.push(item, trace, method, requestMappingInfo, handlerMethod);
//                    break;
//                }
//            }
//
//            if ("OPTIONS".equals(method.name())) {
//                Operation options = item.getOptions();
//                if (options != null) {
//                    this.push(item, options, method, requestMappingInfo, handlerMethod);
//                    break;
//                }
//            }
//
//        }
    }

    public void push(PathItem item, Operation operation, RequestMethod method,
                     RequestMappingInfo info, HandlerMethod handlerMethod) {
        boolean bool = info.getConsumesCondition().getExpressions().isEmpty() && operation.getRequestBody() != null;
        if (bool) {
            consumes(operation.getRequestBody());
        }
        String ok = String.valueOf(HttpStatus.OK.value());
        // 首先判断方法返回值是否为 void 或者 @NoContent Void
        Class<?> returnType = handlerMethod.getReturnType().getParameterType();
        bool = Void.class.isAssignableFrom(returnType)
                || "void".equals(returnType.getName())
                || handlerMethod.hasMethodAnnotation(NoContent.class);
        if (bool) {
            String key = String.valueOf(HttpStatus.NO_CONTENT.value());
            if (operation.getResponses().containsKey(key)) {
                return;
            }
            ApiResponse noContent = new ApiResponse();
            noContent.description("请求成功, 此接口无响应体");
            operation.getResponses().remove(ok);
            operation.getResponses().addApiResponse(key, noContent);
            return;
        }

        // 获取原本的返回值类型
        ApiResponse old = operation.getResponses().get(ok);
        if (old == null) {
            return;
        }

        // 请求成功 默认为200
        old.description("请求成功");
        MediaType all = old.getContent().get(ALL_VALUE);
        if (all == null) {
            return;
        }
        old.getContent().remove(ALL_VALUE);
        info.getProducesCondition().getProducibleMediaTypes().stream().map(m -> m.toString()).collect(Collectors.toSet()).forEach(m -> {
            old.getContent().addMediaType(m, all);
        });
        // 添加响应类型
    }

    protected void consumes(RequestBody body) {
        Content content = body.getContent();
        if (content == null || (!content.containsKey(APPLICATION_JSON_VALUE))) {
            return;
        }
        Schema schema = content.get(APPLICATION_JSON_VALUE).getSchema();

        if (!content.containsKey(APPLICATION_XML_VALUE)) {
            MediaType mediaType = new MediaType();
            mediaType.setSchema(schema);
            content.addMediaType(APPLICATION_XML_VALUE, mediaType);
        }


        if (!content.containsKey(APPLICATION_XML_VALUE)) {
            MediaType mediaType = new MediaType();
            mediaType.setSchema(schema);
            content.addMediaType(APPLICATION_XML_VALUE, mediaType);
        }

        if (!(StringUtils.isEmpty(VND_XML) || content.containsKey(VND_XML))) {
            MediaType mediaType = new MediaType();
            mediaType.setSchema(schema);
            content.addMediaType(VND_XML, mediaType);
        }


        if (!(StringUtils.isEmpty(VND_JSON) || content.containsKey(VND_JSON))) {
            MediaType mediaType = new MediaType();
            mediaType.setSchema(schema);
            content.addMediaType(VND_JSON, mediaType);
        }

    }

}
