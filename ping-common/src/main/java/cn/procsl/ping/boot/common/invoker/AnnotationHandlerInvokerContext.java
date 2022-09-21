package cn.procsl.ping.boot.common.invoker;

import java.lang.annotation.Annotation;

public interface AnnotationHandlerInvokerContext extends HandlerInvokerContext {

    Annotation getAnnotation();

    default <A extends Annotation> A getAnnotation(Class<A> clazz) {
        return (A) this.getAnnotation();
    }

}
