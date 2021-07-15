//package cn.procsl.ping.boot.rest.handler;
//
//import cn.procsl.ping.boot.rest.annotation.Hypermedia;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
//
//class HypermediaBodyAdvice implements ResponseBodyAdvice<Object> {
//
//    @Override
//    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//        return returnType.hasMethodAnnotation(Hypermedia.class);
//    }
//
//    @Override
//    public Object beforeBodyWrite(Object body, MethodParameter returnType,
//                                  MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
//                                  ServerHttpRequest request, ServerHttpResponse response) {
//        return new Root(body);
//    }
//}
