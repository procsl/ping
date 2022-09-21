package cn.procsl.ping.boot.common.invoker;

import lombok.NonNull;

import java.lang.reflect.Parameter;

/**
 * 参数解析器
 */
public interface HandlerArgumentResolver<T extends HandlerInvokerContext> {

    Object[] resolveArgument(T context, Object... args) throws HandlerArgumentResolverException;

    static Object[] createParameterObjectArray(@NonNull Parameter[] parameter) {
        int len = parameter.length;
        if (len == 0) {
            return null;
        }

        return new Object[len];
    }


}
