package cn.procsl.ping.boot.common.event.resolvers;

import cn.procsl.ping.boot.common.event.SubscriberArgumentResolver;
import cn.procsl.ping.boot.common.invoker.AnnotationHandlerInvokerContext;
import com.google.auto.service.AutoService;

import java.lang.reflect.Parameter;
import java.util.EventObject;

@AutoService(SubscriberArgumentResolver.class)
final public class ParameterResolver implements SubscriberArgumentResolver {


    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isSupport(AnnotationHandlerInvokerContext context, Parameter parameter, int index,
                             EventObject object) {
        return EventObject.class.isAssignableFrom(parameter.getType())
                || object.getSource().getClass().isAssignableFrom(parameter.getType());
    }

    @Override
    public Object resolve(AnnotationHandlerInvokerContext context, Parameter parameter, int index, EventObject object) {

        if (EventObject.class.isAssignableFrom(parameter.getType())) {
            return object;
        } else {
            return object.getSource();
        }
    }
}
