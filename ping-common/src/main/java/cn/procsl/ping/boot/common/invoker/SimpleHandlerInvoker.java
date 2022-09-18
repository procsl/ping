package cn.procsl.ping.boot.common.invoker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

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

        Object result;
        try {
            Object[] newArgs = this.resolver.resolveArgument(context, args);
            result = context.getMethod().invoke(context.getHandler(), newArgs);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("方法调用失败:", e);
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            log.error("参数解析错误:", e);
            throw e;
        }
        return result;
    }


}
