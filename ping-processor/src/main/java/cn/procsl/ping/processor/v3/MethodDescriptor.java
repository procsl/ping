package cn.procsl.ping.processor.v3;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public interface MethodDescriptor<P extends MethodParameterDescriptor, R extends MethodReturnValueDescriptor> {

    String getMethodName();

    ExecutableElement getTargetElement();

    List<String> getThrows();

    P getRequestParameterDescriptor();

    R getRequestReturnDescriptor();
}
