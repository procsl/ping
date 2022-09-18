package cn.procsl.ping.boot.connect.server;

import cn.procsl.ping.boot.common.invoker.AnnotationHandlerInvokerContext;
import io.socket.socketio.server.SocketIoSocket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor
public class SocketIOConnectContext implements AnnotationHandlerInvokerContext {

    final AnnotationHandlerInvokerContext context;

    final Object[] connectionArgs;


    public SocketIoSocket getSocketIoSocket() {
        return (SocketIoSocket) connectionArgs[0];
    }

    @Override
    public Annotation getAnnotation() {
        return context.getAnnotation();
    }

    @Override
    public Object getHandler() {
        return context.getHandler();
    }

    @Override
    public Method getMethod() {
        return context.getMethod();
    }
}
