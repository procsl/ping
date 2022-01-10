package cn.procsl.ping.processor.v3.web;

import cn.procsl.ping.processor.v3.ProcessorEnvironment;
import cn.procsl.ping.processor.v3.MethodDescriptor;

import java.io.IOException;
import java.util.Collection;

public interface JavaSourceGenerator {

    void generated(Collection<MethodDescriptor> descriptors, ProcessorEnvironment environment) throws IOException;

}
