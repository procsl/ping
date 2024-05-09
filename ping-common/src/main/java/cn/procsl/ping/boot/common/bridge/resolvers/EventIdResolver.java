package cn.procsl.ping.boot.common.bridge.resolvers;

import cn.procsl.ping.boot.common.ID;
import cn.procsl.ping.boot.common.Subscriber;
import cn.procsl.ping.boot.common.bridge.SubscriberArgumentResolver;
import cn.procsl.ping.boot.common.invoker.AnnotationHandlerInvokerContext;
import com.google.auto.service.AutoService;

import java.lang.reflect.Parameter;
import java.util.EventObject;

@AutoService(SubscriberArgumentResolver.class)
final public class EventIdResolver implements SubscriberArgumentResolver {


    @Override
    public boolean isSupport(AnnotationHandlerInvokerContext context, Parameter parameter, int index,
                             EventObject object) {
        return object instanceof ID && String.class == parameter.getType() && parameter.getAnnotation(
                Subscriber.EventId.class) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolve(AnnotationHandlerInvokerContext context, Parameter parameter, int index,
                          EventObject object) {
        return ((ID<String>) object).getId();
    }
}
