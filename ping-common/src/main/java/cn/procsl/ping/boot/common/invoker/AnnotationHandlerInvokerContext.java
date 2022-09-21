package cn.procsl.ping.boot.common.invoker;

import java.lang.annotation.Annotation;

public interface AnnotationHandlerInvokerContext extends HandlerInvokerContext {

    Annotation getAnnotation();

    @SuppressWarnings({"unchecked", "unused"})
    default <A extends Annotation> A getAnnotation(Class<A> clazz) {
        return (A) this.getAnnotation();
    }

}
