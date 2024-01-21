package cn.procsl.ping.boot.web.component;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;

public interface AbstractMethodAnnotationInterceptor<A extends Annotation> extends HandlerInterceptor {

    Class<A> getAnnotationClass();

    @Override
    default boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        boolean isHandler = handler instanceof HandlerMethod;
        if (!isHandler) {
            return true;
        }
        A annotation = ((HandlerMethod) handler).getMethodAnnotation(this.getAnnotationClass());
        return this.doPreHandle(request, response, (HandlerMethod) handler, annotation);
    }

    boolean doPreHandle(HttpServletRequest request, HttpServletResponse response,
                        HandlerMethod handler, A annotation);
}
