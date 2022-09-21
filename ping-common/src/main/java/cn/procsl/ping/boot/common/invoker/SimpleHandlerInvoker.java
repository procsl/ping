package cn.procsl.ping.boot.common.invoker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

@Slf4j
public final class SimpleHandlerInvoker<T extends HandlerInvokerContext> implements
        HandlerInvoker<T> {

    @Getter
    final T context;
    final HandlerArgumentResolver<T> resolver;

    public SimpleHandlerInvoker(T context, HandlerArgumentResolver<T> resolver) {
        this.context = context;
        this.resolver = resolver;
    }

    @Override
    public Object invoke(Object... args) {
        Object[] newArgs = this.resolver.resolveArgument(context, args);
        return ReflectionUtils.invokeMethod(this.context.getMethod(), this.getContext().getHandler(), newArgs);
    }


}
