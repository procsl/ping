package cn.procsl.ping.boot.connect.server;

import java.lang.reflect.Parameter;

public interface SocketIOHandlerArgumentResolver {

    boolean supportsParameter(SocketIOConnectContext context, int index, Parameter parameter, Object[] args);

    Object resolveArgument(SocketIOConnectContext context, int index, Parameter parameter, Object[] args);

}
