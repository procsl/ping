package cn.procsl.ping.boot.connect.resolver;

import cn.procsl.ping.boot.connect.server.Namespace;
import cn.procsl.ping.boot.connect.server.NamespaceEventHandlerResolver;
import cn.procsl.ping.boot.connect.server.SocketIOConnectContext;
import com.google.auto.service.AutoService;
import io.socket.engineio.server.Emitter;

import java.lang.annotation.Annotation;

@AutoService(NamespaceEventHandlerResolver.class)
public class EventResolver implements NamespaceEventHandlerResolver {
    @Override
    public Class<? extends Annotation> getNamespaceAnnotation() {
        return Namespace.Event.class;
    }

    @Override
    public void processor(Annotation annotation, SocketIOConnectContext invokeContext, Emitter.Listener func) {
        Namespace.Event event = (Namespace.Event) annotation;
        invokeContext.getSocketIoSocket().on(event.value(), func);
    }

}
