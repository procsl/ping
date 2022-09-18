package cn.procsl.ping.boot.common.invoker;

import java.lang.annotation.Annotation;

public interface AnnotationHandlerInvokerContext extends HandlerInvokerContext {

    Annotation getAnnotation();

}
