package cn.procsl.ping.boot.connect.resolver;

import cn.procsl.ping.boot.connect.server.Namespace;
import cn.procsl.ping.boot.connect.server.NamespaceEventHandlerResolver;
import cn.procsl.ping.boot.connect.server.SocketIOConnectContext;
import com.google.auto.service.AutoService;
import io.socket.engineio.server.Emitter;

import java.lang.annotation.Annotation;

@AutoService(NamespaceEventHandlerResolver.class)
public class ConnectEventResolver implements NamespaceEventHandlerResolver {
    @Override
    public Class<? extends Annotation> getNamespaceAnnotation() {
        return Namespace.Connect.class;
    }

    @Override
    public void processor(Annotation annotation, SocketIOConnectContext invokeContext, Emitter.Listener func) {
        invokeContext.getSocketIoSocket().once(Namespace.Connect.connect, func);
    }
}
