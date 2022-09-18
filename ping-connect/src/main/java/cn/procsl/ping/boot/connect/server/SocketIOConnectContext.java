package cn.procsl.ping.boot.connect.server;

import io.socket.socketio.server.SocketIoSocket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor
public class SocketIOConnectContext {

    final private Object target;

    final private Method method;

    final private Object[] connectionArgs;


    void invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        method.invoke(target, args);
    }

    public SocketIoSocket getSocketIoSocket() {
        return (SocketIoSocket) connectionArgs[0];
    }

}
