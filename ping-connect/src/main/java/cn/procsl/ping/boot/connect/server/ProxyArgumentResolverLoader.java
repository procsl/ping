package cn.procsl.ping.boot.connect.server;

import cn.procsl.ping.boot.common.load.ServiceProxyLoader;

import java.lang.reflect.Parameter;

final class ProxyArgumentResolverLoader extends ServiceProxyLoader<SocketIOHandlerArgumentResolver> implements
        SocketIOHandlerArgumentResolver {
    ProxyArgumentResolverLoader() {
        super(SocketIOHandlerArgumentResolver.class);
        this.load();
    }

    @Override
    public boolean supportsParameter(SocketIOConnectContext context, int index, Parameter parameter, Object[] args) {
        throw new UnsupportedOperationException("不支持的调用");
    }

    @Override
    public Object resolveArgument(SocketIOConnectContext context, int index, Parameter parameter, Object[] args) {
        for (SocketIOHandlerArgumentResolver resolver : this.service) {
            if (!resolver.supportsParameter(context, index, parameter, args)) {
                continue;
            }
            return resolver.resolveArgument(context, index, parameter, args);
        }
        return null;
    }
}
