package cn.procsl.ping.boot.common.invoker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;

@RequiredArgsConstructor
public final class SimpleHandlerInvoker<T extends HandlerInvokerContext> implements HandlerInvoker<T> {

    @Getter
    final T context;
    final HandlerArgumentResolver resolver;

    @Override
    public Object invoke(Object... args) {

        Object[] newArgs = this.resolver.resolveArgument(args);
        Object result;

        try {
            result = context.getMethod().invoke(context.getHandler(), (Object) newArgs);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


}
