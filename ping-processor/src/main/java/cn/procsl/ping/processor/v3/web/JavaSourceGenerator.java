package cn.procsl.ping.processor.v3.web;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.v3.web.descriptor.RequestMethodDescriptor;

import java.io.IOException;
import java.util.Collection;

public interface JavaSourceGenerator {

    void generated(Collection<RequestMethodDescriptor> descriptors, ProcessorEnvironment environment) throws IOException;

}
