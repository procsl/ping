package cn.procsl.ping.processor.v3;

import java.util.Collection;
import java.util.List;

public interface MethodDescriptor extends Descriptor {

    String getMethodName();

    List<TypeNameDescriptor> getThrows();

    Collection<MethodParameterDescriptor> getParameterDescriptor();

    MethodReturnValueDescriptor getReturnDescriptor();

    SourceCodeDescriptor getBody();
}
