package cn.procsl.ping.processor.v3;

import java.io.IOException;
import java.util.Collection;

public interface JavaSourceGenerator {

    void generated(Collection<TypeDescriptor> descriptors, ProcessorEnvironment environment) throws IOException;

}
