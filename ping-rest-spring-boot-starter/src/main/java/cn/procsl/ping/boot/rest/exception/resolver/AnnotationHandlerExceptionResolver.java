//package cn.procsl.ping.boot.rest.exception.resolver;
//
//import cn.procsl.ping.boot.rest.config.RestWebProperties;
//import cn.procsl.ping.boot.rest.exception.ExceptionCode;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Objects;
//
//@Slf4j
//public class AnnotationHandlerExceptionResolver extends AbstractHandlerExceptionResolver {
//    /**
//     * Actually resolve the given exception that got thrown during handler execution,
//     * returning a {@link ModelAndView} that represents a specific error page if appropriate.
//     * <p>May be overridden in subclasses, in order to apply specific exception checks.
//     * Note that this template method will be invoked <i>after</i> checking whether this
//     * resolved applies ("mappedHandlers" etc), so an implementation may simply proceed
//     * with its actual exception handling.
//     *
//     * @param request  current HTTP request
//     * @param response current HTTP response
//     * @param handler  the executed handler, or {@code null} if none chosen at the time
//     *                 of the exception (for example, if multipart resolution failed)
//     * @param ex       the exception that got thrown during handler execution
//     * @return a corresponding {@code ModelAndView} to forward to,
//     * or {@code null} for default processing in the resolution chain
//     */
//    @Override
//    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        if (!(handler instanceof HandlerMethod)) {
//            log.warn("Handler is not HandlerMethod:{}", handler);
//            return null;
//        }
//        log.error("注解异常处理器:", ex);
//
//        HandlerMethod method = (HandlerMethod) handler;
//
//        if (method.hasMethodAnnotation(ExceptionHandler.class)) {
//            ExceptionHandler convert = method.getMethodAnnotation(ExceptionHandler.class);
//            ModelAndView tmp = errorConvert(ex, convert);
//            if (tmp != null) {
//                return tmp;
//            }
//        }
//
//        if (!method.hasMethodAnnotation(ExceptionHandlers.class)) {
//            return null;
//        }
//
//        ExceptionHandler[] converts = Objects.requireNonNull(method.getMethodAnnotation(ExceptionHandlers.class)).value();
//        for (ExceptionHandler convert : converts) {
//            ModelAndView tmp = errorConvert(ex, convert);
//            if (tmp != null) {
//                return tmp;
//            }
//        }
//        return null;
//    }
//
//    private ModelAndView errorConvert(Exception ex, ExceptionHandler methodAnnotation) {
//        for (Class<? extends Exception> exception : methodAnnotation.exceptions()) {
//            if (this.matcher(methodAnnotation.type(), ex, exception)) {
//                ModelAndView mv = new ModelAndView();
//                ExceptionCode exp = new ExceptionCode();
//                mv.addObject(RestWebProperties.modelKey, exp);
//                mv.setStatus(methodAnnotation.status());
//                exp.setCode(methodAnnotation.code());
//                exp.setMessage(methodAnnotation.message().isEmpty() ? ex.getMessage() : methodAnnotation.message());
//                return mv;
//            }
//        }
//        return null;
//    }
//
//    private boolean matcher(ExceptionHandler.MatcherType type, Exception ex, Class<? extends Exception> exception) {
//        if (type == ExceptionHandler.MatcherType.equals) {
//            return ex.getClass() == exception;
//        }
//        return exception.isAssignableFrom(ex.getClass());
//    }
//
//}
