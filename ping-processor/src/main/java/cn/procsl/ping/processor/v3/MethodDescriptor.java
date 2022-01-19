package cn.procsl.ping.processor.v3;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public interface MethodDescriptor {

    String getMethodName();

    ExecutableElement getTargetElement();

    List<String> getThrows();

    MethodParameterDescriptor getParameterDescriptor();

    MethodReturnValueDescriptor getReturnDescriptor();
}
