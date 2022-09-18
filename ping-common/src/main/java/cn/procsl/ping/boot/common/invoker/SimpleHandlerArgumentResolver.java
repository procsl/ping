package cn.procsl.ping.boot.common.invoker;

public final class SimpleHandlerArgumentResolver implements HandlerArgumentResolver {

    @Override
    public Object[] resolveArgument(Object... args) throws HandlerArgumentResolverException {
        return new Object[0];
    }

}
