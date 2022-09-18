package cn.procsl.ping.boot.common.invoker;

/**
 * 泛化调用接口
 */
public interface HandlerInvoker<T extends HandlerInvokerContext> {

    Object invoke(Object... args);

    T getContext();

}
