package cn.procsl.ping.boot.connect.server;

import cn.procsl.ping.boot.common.invoker.HandlerInvoker;
import cn.procsl.ping.boot.common.load.AbstractServiceProxyLoader;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
final class NamespaceEventHandlerResolverLoader extends
        AbstractServiceProxyLoader<NamespaceEventHandlerResolver> {

    public NamespaceEventHandlerResolverLoader() {
        super(NamespaceEventHandlerResolver.class);
    }

    public Collection<Class<? extends Annotation>> getNamespaceAnnotations() {
        return this.getService().stream().map(NamespaceEventHandlerResolver::getNamespaceAnnotation)
                   .collect(Collectors.toSet());
    }

    public void processor(SocketIOConnectContext invokeContext, HandlerInvoker<SocketIOConnectContext> func) {
        for (NamespaceEventHandlerResolver resolver : this.getService()) {
            boolean isSupport = resolver.support(invokeContext.getAnnotation());
            if (!isSupport) {
                continue;
            }
            resolver.processor(invokeContext.getAnnotation(), invokeContext, func::invoke);
        }
    }
}
