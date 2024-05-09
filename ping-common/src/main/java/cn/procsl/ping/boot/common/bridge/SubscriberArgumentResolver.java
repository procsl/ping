package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.invoker.AnnotationHandlerInvokerContext;

import java.lang.reflect.Parameter;
import java.util.EventObject;

public interface SubscriberArgumentResolver {

    boolean isSupport(AnnotationHandlerInvokerContext context, Parameter parameter, int index, EventObject object);

    Object resolve(AnnotationHandlerInvokerContext context, Parameter parameter, int index, EventObject object);

    default int order() {
        return 0;
    }

}
