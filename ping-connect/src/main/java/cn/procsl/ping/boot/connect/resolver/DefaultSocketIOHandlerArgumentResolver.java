package cn.procsl.ping.boot.connect.resolver;


import cn.procsl.ping.boot.connect.server.SocketIOConnectContext;
import cn.procsl.ping.boot.connect.server.SocketIOHandlerArgumentResolver;
import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Parameter;

@Slf4j
@AutoService(SocketIOHandlerArgumentResolver.class)
public class DefaultSocketIOHandlerArgumentResolver implements SocketIOHandlerArgumentResolver {

    @Override
    public boolean supportsParameter(SocketIOConnectContext context, int index, Parameter parameter,
                                     Object[] args) {
        if (args == null || args.length == 0) {
            return false;
        }

        return parameter.isVarArgs() || parameter.getType().isAssignableFrom(Object[].class);
    }

    @Override
    public Object resolveArgument(SocketIOConnectContext context, int index, Parameter parameter,
                                  Object[] args) {

        return args;
    }
}
