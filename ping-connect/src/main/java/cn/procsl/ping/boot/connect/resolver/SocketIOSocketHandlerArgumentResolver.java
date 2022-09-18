package cn.procsl.ping.boot.connect.resolver;

import cn.procsl.ping.boot.connect.server.SocketIOConnectContext;
import com.google.auto.service.AutoService;
import io.socket.socketio.server.SocketIoSocket;

import java.lang.reflect.Parameter;

@AutoService(cn.procsl.ping.boot.connect.server.SocketIOHandlerArgumentResolver.class)
public class SocketIOSocketHandlerArgumentResolver implements
        cn.procsl.ping.boot.connect.server.SocketIOHandlerArgumentResolver {

    @Override
    public boolean supportsParameter(SocketIOConnectContext context, int index, Parameter parameter,
                                     Object[] args) {
        return parameter.getType().isAssignableFrom(SocketIoSocket.class);
    }

    @Override
    public Object resolveArgument(SocketIOConnectContext context, int index, Parameter parameter, Object[] args) {
        return context.getConnectionArgs()[0];
    }
}
