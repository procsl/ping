package cn.procsl.ping.processor.v3;

import java.io.IOException;
import java.util.Collection;

public interface JavaSourceGenerator<P extends MethodParameterDescriptor, R extends MethodReturnValueDescriptor> {

    void generated(Collection<MethodDescriptor> descriptors, ProcessorEnvironment environment) throws IOException;

}
