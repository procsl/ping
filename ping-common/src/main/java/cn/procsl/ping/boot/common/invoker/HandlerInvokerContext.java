package cn.procsl.ping.boot.common.invoker;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface HandlerInvokerContext {

    @NotNull Object getHandler();

    @NotNull Method getMethod();

    default @NotNull Parameter[] getParameters() {
        return this.getMethod().getParameters();
    }

}
