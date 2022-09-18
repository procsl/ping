package cn.procsl.ping.boot.common.invoker;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;

public interface HandlerInvokerContext {

    @NotNull Object getHandler();

    @NotNull Method getMethod();

}
