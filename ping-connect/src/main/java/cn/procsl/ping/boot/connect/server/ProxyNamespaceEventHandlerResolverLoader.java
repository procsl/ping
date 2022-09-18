package cn.procsl.ping.boot.connect.server;

import cn.procsl.ping.boot.common.load.ServiceProxyLoader;
import io.socket.engineio.server.Emitter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;

final class ProxyNamespaceEventHandlerResolverLoader extends
        ServiceProxyLoader<NamespaceEventHandlerResolver> implements
        NamespaceEventHandlerResolver {

    public ProxyNamespaceEventHandlerResolverLoader() {
        super(NamespaceEventHandlerResolver.class);
    }

    @Override
    public Class<? extends Annotation> getNamespaceAnnotation() {
        throw new UnsupportedOperationException("不允许的操作");
    }

    public Collection<Class<? extends Annotation>> getNamespaceAnnotations() {
        return this.service.stream().map(NamespaceEventHandlerResolver::getNamespaceAnnotation)
                           .collect(Collectors.toSet());
    }

    @Override
    public void processor(Annotation annotation, SocketIOConnectContext invokeContext, Emitter.Listener func) {
        for (NamespaceEventHandlerResolver resolver : this.service) {
            boolean isSupport = resolver.support(annotation);
            if (!isSupport) {
                continue;
            }
            resolver.processor(annotation, invokeContext, func);
        }
    }
}
