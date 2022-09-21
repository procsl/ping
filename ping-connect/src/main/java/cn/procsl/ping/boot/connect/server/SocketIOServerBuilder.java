package cn.procsl.ping.boot.connect.server;

import cn.procsl.ping.boot.common.invoker.AnnotationHandlerInvokerContext;
import cn.procsl.ping.boot.common.invoker.MultiScannerAnnotationHandlerResolver;
import cn.procsl.ping.boot.common.invoker.SimpleHandlerInvoker;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public final class SocketIOServerBuilder {

    final NamespaceEventHandlerResolverLoader handlerResolverLoader =
            new NamespaceEventHandlerResolverLoader();
    EngineIoServerOptions config = EngineIoServerOptions.newFromDefault();
    EngineIoServer server;
    SocketIoServer socketIoServer;
    Collection<Object> handlers = new ArrayList<>();
    ArgumentResolverLoader argumentResolver = new ArgumentResolverLoader();

    MultiScannerAnnotationHandlerResolver scannerAnnotationHandlerResolver;

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
            log.info("Init namespace: {}", handler.getClass());
        }
        return socketIoServer.namespace(namespace.name());
    }

    void namespaceConnectRegister(SocketIoNamespace instance, Object handler) {
        Collection<AnnotationHandlerInvokerContext> events =
                this.scannerAnnotationHandlerResolver.resolve(handler);

        instance.on("connection", args -> {
            SocketIoSocket socket = (SocketIoSocket) args[0];
            log.debug("connect to namespace:{}, id:{}", socket.getNamespace().getName(), socket.getId());
            try {
                for (AnnotationHandlerInvokerContext namespace : events) {
                    SocketIOConnectContext context = new SocketIOConnectContext(namespace, args);
                    SimpleHandlerInvoker<SocketIOConnectContext> invoker = new SimpleHandlerInvoker<>(context,
                            this.argumentResolver);
                    this.handlerResolverLoader.processor(context, invoker);
                }
            } catch (Exception e) {
                log.error("出现错误:", e);
                throw e;
            }
            socket.emit(Namespace.Connect.connect);
        });
    }

    void socketIoNamespaceAwareRegister(Object v, SocketIoNamespace instance) {
        if (v instanceof SocketIONamespaceAware) {
            ((SocketIONamespaceAware) v).setSocketIoNamespace(instance);
        }
    }

    public EngineIoServer build() {
        this.server = new EngineIoServer(config);
        this.socketIoServer = new SocketIoServer(this.server);
        this.handlerResolverLoader.load();
        this.argumentResolver.load();
        this.scannerAnnotationHandlerResolver = new MultiScannerAnnotationHandlerResolver(
                handlerResolverLoader.getNamespaceAnnotations());
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
