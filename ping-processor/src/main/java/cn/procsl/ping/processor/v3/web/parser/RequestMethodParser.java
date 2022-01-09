package cn.procsl.ping.processor.v3.web.parser;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.v3.web.descriptor.RequestMethodDescriptor;

import javax.lang.model.element.ExecutableElement;

public class RequestMethodParser {

    public RequestMethodDescriptor parser(ExecutableElement element, ProcessorEnvironment environment) {


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
