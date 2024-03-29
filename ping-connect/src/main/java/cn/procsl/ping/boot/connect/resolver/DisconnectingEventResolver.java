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
public class DisconnectingEventResolver implements NamespaceEventHandlerResolver {

    @Override
    public Class<? extends Annotation> getNamespaceAnnotation() {
        return Namespace.Disconnecting.class;
    }

    @Override
    public void processor(Annotation annotation, SocketIOConnectContext invokeContext, Emitter.Listener func) {
        log.debug("register {} event", Namespace.Disconnecting.disconnecting);
        invokeContext.getSocketIoSocket().once(Namespace.Disconnecting.disconnecting, func);
    }

}
