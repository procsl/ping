package cn.procsl.ping.boot.connect.server;

import cn.procsl.ping.boot.common.invoker.HandlerArgumentResolver;
import cn.procsl.ping.boot.common.invoker.HandlerArgumentResolverException;
import cn.procsl.ping.boot.common.load.AbstractServiceProxyLoader;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Parameter;

@Slf4j
final class ArgumentResolverLoader extends AbstractServiceProxyLoader<SocketIOHandlerArgumentResolver> implements
        HandlerArgumentResolver<SocketIOConnectContext> {

    ArgumentResolverLoader() {
        super(SocketIOHandlerArgumentResolver.class);
    }

    @Override
    public Object[] resolveArgument(SocketIOConnectContext context, Object... args)
            throws HandlerArgumentResolverException {

        if (log.isTraceEnabled()) {
            log.trace("解析方法: [{}.{}]", context.getHandler().getClass().getName(),
                    context.getMethod().getName());
        }

        @NotNull Parameter[] parameters = context.getParameters();
        Object[] newArgs = HandlerArgumentResolver.createParameterObjectArray(parameters);

        if (newArgs == null) {
            return null;
        }

        for (int i = 0; i < parameters.length; i++) {
            newArgs[i] = resolveParameter(context, i, parameters[i], args);
        }
        log.trace("解析后的参数为:[{}]", newArgs);
        return newArgs;
    }

    Object resolveParameter(SocketIOConnectContext context, int i, Parameter parameters, Object[] args) {
        for (SocketIOHandlerArgumentResolver resolver : this.getService()) {
            if (!resolver.supportsParameter(context, i, parameters, args)) {
                continue;
            }
            return resolver.resolveArgument(context, i, parameters, args);
        }
        return null;
    }
}
