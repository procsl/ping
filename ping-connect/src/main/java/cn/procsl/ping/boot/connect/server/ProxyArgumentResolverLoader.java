package cn.procsl.ping.boot.connect.server;

import cn.procsl.ping.boot.common.invoker.HandlerArgumentResolver;
import cn.procsl.ping.boot.common.invoker.HandlerArgumentResolverException;
import cn.procsl.ping.boot.common.load.ServiceProxyLoader;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Parameter;

@Slf4j
final class ProxyArgumentResolverLoader extends ServiceProxyLoader<SocketIOHandlerArgumentResolver> implements
        HandlerArgumentResolver<SocketIOConnectContext> {

    ProxyArgumentResolverLoader() {
        super(SocketIOHandlerArgumentResolver.class);
    }

    @Override
    public Object[] resolveArgument(SocketIOConnectContext context, Object... args)
            throws HandlerArgumentResolverException {

        Parameter[] parameters = context.getMethod().getParameters();

        log.trace("方法: [{}#{}],参数数量为:{}", context.getHandler().getClass().getName(),
                context.getMethod().getName(), parameters.length);
        if (parameters.length == 0) {
            return null;
        }

        Object[] newArgs = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            newArgs[i] = resolveParameter(context, i, parameters[i], args);
        }
        log.trace("解析后的参数为:[{}]", newArgs);
        return newArgs;
    }

    Object resolveParameter(SocketIOConnectContext context, int i, Parameter parameters, Object[] args) {
        for (SocketIOHandlerArgumentResolver resolver : this.service) {
            if (!resolver.supportsParameter(context, i, parameters, args)) {
                continue;
            }
            return resolver.resolveArgument(context, i, parameters, args);
        }
        return null;
    }
}
