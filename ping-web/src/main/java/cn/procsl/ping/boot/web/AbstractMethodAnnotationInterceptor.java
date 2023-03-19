package cn.procsl.ping.boot.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

@RequiredArgsConstructor
public abstract class AbstractMethodAnnotationInterceptor<A extends Annotation> implements HandlerInterceptor {

    final Class<A> annotationClass;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        boolean isHandler = handler instanceof HandlerMethod;
        if (!isHandler) {
            return true;
        }
        A annotation = ((HandlerMethod) handler).getMethodAnnotation(annotationClass);
        return this.doPreHandle(request, response, (HandlerMethod) handler, annotation);
    }

    protected abstract boolean doPreHandle(HttpServletRequest request, HttpServletResponse response,
                                           HandlerMethod handler, A annotation);
}
