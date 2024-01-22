package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.invoker.AnnotationHandlerInvokerContext;
import cn.procsl.ping.boot.common.invoker.HandlerArgumentResolver;
import cn.procsl.ping.boot.common.invoker.HandlerArgumentResolverException;
import cn.procsl.ping.boot.common.utils.AbstractServiceProxyLoader;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;

final class ArgumentResolverLoader extends
        AbstractServiceProxyLoader<SubscriberArgumentResolver> implements
        HandlerArgumentResolver<AnnotationHandlerInvokerContext> {

    final ApplicationContext context;

    public ArgumentResolverLoader(ApplicationContext context) {
        super(SubscriberArgumentResolver.class);
        this.context = context;
    }

    @Override
    protected List<SubscriberArgumentResolver> onLoad(List<SubscriberArgumentResolver> tmp) {
        tmp.sort(Comparator.comparingInt(SubscriberArgumentResolver::order));
        return tmp;
    }


    @Override
    public Object[] resolveArgument(AnnotationHandlerInvokerContext context, Object... args)
            throws HandlerArgumentResolverException {
        Parameter[] parameters = context.getParameters();
        Object[] arr = HandlerArgumentResolver.createParameterObjectArray(parameters);
        if (arr == null) {
            return null;
        }

        EventObject value = (EventObject) (args[0]);

        for (int i = 0; i < parameters.length; i++) {
            arr[i] = extracted(context, parameters, value, i);
        }
        return arr;
    }

    Object extracted(AnnotationHandlerInvokerContext context, Parameter[] parameters, EventObject value, int i) {
        for (SubscriberArgumentResolver resolver : this.getService()) {
            if (resolver.isSupport(context, parameters[i], i, value)) {
                return resolver.resolve(context, parameters[i], i, value);
            }
        }
        return null;
    }

}
