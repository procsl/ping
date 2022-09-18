package cn.procsl.ping.boot.connect.resolver;

import cn.procsl.ping.boot.connect.server.Namespace;
import cn.procsl.ping.boot.connect.server.NamespaceEventHandlerResolver;
import cn.procsl.ping.boot.connect.server.SocketIOConnectContext;
import com.google.auto.service.AutoService;
import io.socket.engineio.server.Emitter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;

@Slf4j
@AutoService(NamespaceEventHandlerResolver.class)
public class EventResolver implements NamespaceEventHandlerResolver {
    @Override
    public Class<? extends Annotation> getNamespaceAnnotation() {
        return Namespace.Event.class;
    }

    @Override
    public void processor(Annotation annotation, SocketIOConnectContext invokeContext, Emitter.Listener func) {
        Namespace.Event event = (Namespace.Event) annotation;
        log.debug("register event: [{}]", event.value());
        invokeContext.getSocketIoSocket().on(event.value(), func);
    }

}
