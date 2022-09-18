package cn.procsl.ping.boot.common.invoker;

/**
 * 参数解析器
 */
public interface HandlerArgumentResolver<T extends HandlerInvokerContext> {

    Object[] resolveArgument(T context, Object... args) throws HandlerArgumentResolverException;

}
