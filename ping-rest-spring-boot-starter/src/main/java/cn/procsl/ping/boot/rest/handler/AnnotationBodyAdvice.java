//package cn.procsl.ping.boot.rest.handler;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
//
//
//@Slf4j
////@ControllerAdvice
//public class AnnotationBodyAdvice implements ResponseBodyAdvice<Object> {
//
//    final NoContentBodyAdvice noContentBodyAdvice = new NoContentBodyAdvice();
//
//    final HypermediaBodyAdvice hypermediaBodyAdvice = new HypermediaBodyAdvice();
//
//    final WrapperBodyAdvice wrapperBodyAdvice = new WrapperBodyAdvice();
////
////    @Override
////    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
////        Class<?> contain = returnType.getContainingClass();
////        return restEndpoint != null;
////    }
//
////    @Override
////    public Object beforeBodyWrite(Object body,
////                                  MethodParameter returnType,
////                                  MediaType selectedContentType,
////                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
////                                  ServerHttpRequest request,
////                                  ServerHttpResponse response) {
////
////        Ok ok = returnType.getMethodAnnotation(Ok.class);
////        if (ok != null) {
////            response.setStatusCode(ok.status());
////        }
////
////        boolean isSupports = noContentBodyAdvice.supports(returnType, selectedConverterType);
////        if (isSupports) {
////            return noContentBodyAdvice.beforeBodyWrite(body, returnType, selectedContentType, selectedConverterType, request, response);
////        }
////
////        isSupports = hypermediaBodyAdvice.supports(returnType, selectedConverterType);
////        if (isSupports) {
////            return hypermediaBodyAdvice.beforeBodyWrite(body, returnType, selectedContentType, selectedConverterType, request, response);
////        }
////
////        isSupports = wrapperBodyAdvice.supports(returnType, selectedConverterType);
////        if (isSupports) {
////            return wrapperBodyAdvice.beforeBodyWrite(body, returnType, selectedContentType, selectedConverterType, request, response);
////        }
////
////        return body;
////    }
//}
