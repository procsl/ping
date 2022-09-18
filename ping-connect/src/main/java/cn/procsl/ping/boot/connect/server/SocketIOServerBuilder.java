package cn.procsl.ping.boot.connect.server;

import io.socket.engineio.server.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public final class SocketIOServerBuilder {

    final ProxyNamespaceEventHandlerResolverLoader resolverProxy = new ProxyNamespaceEventHandlerResolverLoader();
    EngineIoServerOptions config = EngineIoServerOptions.newFromDefault();
    EngineIoServer server;
    SocketIoServer socketIoServer;
    Collection<Object> handlers = new ArrayList<>();
    HandlerInvoker handlerInvoker;

    private SocketIOServerBuilder() {
    }

    public static SocketIOServerBuilder builder() {
        return new SocketIOServerBuilder();
    }

    SocketIoNamespace namespaceRegister(Object handler) {
        Namespace namespace = AnnotationUtils.findAnnotation(handler.getClass(), Namespace.class);
        if (namespace == null) {
            throw new IllegalStateException("@Namespace annotation not found!");
        } else {
            log.info("init namespace: {}", handler.getClass());
        }
        return socketIoServer.namespace(namespace.name());
    }

    void namespaceConnectRegister(SocketIoNamespace instance, Object handler) {

        List<List<Map<String, Object>>> events = this.handlerInvoker.findEventHandler(handler);

        instance.on("connection", args -> {
            SocketIoSocket socket = (SocketIoSocket) args[0];
            log.info("新连接:namespace:{}, id:{}", socket.getNamespace().getName(), socket.getId());
            try {
                for (List<Map<String, Object>> namespace : events) {
                    connectionRegister(namespace, args);
                }
            } catch (Exception e) {
                log.error("出现错误:", e);
                throw e;
            }
            socket.emit(Namespace.Connect.connect);
        });
    }

    void connectionRegister(List<Map<String, Object>> events, Object[] args) {
        for (Map<String, Object> event : events) {
            SocketIOConnectContext invokeContext = new SocketIOConnectContext(event.get("handler"),
                    (Method) event.get("method"), args);
            Emitter.Listener func = item -> this.handlerInvoker.invoke(invokeContext, item);
            resolverProxy.processor((Annotation) event.get("annotation"), invokeContext, func);
        }
    }

    void socketIoNamespaceAwareRegister(Object v, SocketIoNamespace instance) {
        if (v instanceof SocketIONamespaceAware) {
            ((SocketIONamespaceAware) v).setSocketIoNamespace(instance);
        }
    }

    public EngineIoServer build() {
        this.server = new EngineIoServer(config);
        this.socketIoServer = new SocketIoServer(this.server);
        this.resolverProxy.load();
        this.handlerInvoker = new HandlerInvoker(resolverProxy.getNamespaceAnnotations());
        for (Object handler : this.handlers) {
            SocketIoNamespace instance = this.namespaceRegister(handler);
            socketIoNamespaceAwareRegister(handler, instance);
            namespaceConnectRegister(instance, handler);
        }
        return this.server;
    }

    @SuppressWarnings("unused")
    public SocketIOServerBuilder options(@NonNull EngineIoServerOptions options) {
        this.config = options;
        return this;
    }

    @SuppressWarnings("unused")
    public SocketIOServerBuilder addHandlers(@NonNull Object handler) {
        this.handlers.add(handler);
        return this;
    }
}
