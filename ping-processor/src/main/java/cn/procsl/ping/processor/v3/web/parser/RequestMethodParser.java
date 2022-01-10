package cn.procsl.ping.processor.v3.web.parser;

import cn.procsl.ping.processor.v3.ProcessorEnvironment;
import cn.procsl.ping.processor.v3.MethodDescriptor;

import javax.lang.model.element.ExecutableElement;

public class RequestMethodParser {

    public MethodDescriptor parser(ExecutableElement element, ProcessorEnvironment environment) {


        if (!environment.isPublic(element)) {
            return null;
        }

        if (environment.isStatic(element)) {
            return null;
        }

        MethodDescriptorImpl descriptor = new MethodDescriptorImpl(element, environment);
        if (descriptor.isRequest()) {
            return descriptor;
        }

        return null;
    }

}
