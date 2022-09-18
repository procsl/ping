package cn.procsl.ping.boot.common.invoker;

import java.util.Collection;

public interface HandlerResolver<T extends HandlerInvokerContext> {

    Collection<T> resolve(Object target);

}
