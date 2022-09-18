package cn.procsl.ping.boot.common.invoker;

/**
 * 参数解析器
 */
public interface HandlerArgumentResolver {

    Object[] resolveArgument(Object... args) throws HandlerArgumentResolverException;

}
