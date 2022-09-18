package cn.procsl.ping.boot.connect.server;

import io.socket.engineio.server.Emitter;

import java.lang.annotation.Annotation;

public interface NamespaceEventHandlerResolver {

    Class<? extends Annotation> getNamespaceAnnotation();

    default boolean support(Annotation annotation) {
        return this.getNamespaceAnnotation().isAssignableFrom(annotation.getClass());
    }

    void processor(Annotation annotation, SocketIOConnectContext invokeContext, Emitter.Listener func);

}
