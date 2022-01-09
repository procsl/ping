package cn.procsl.ping.processor.v3.web.parser;

import cn.procsl.ping.processor.v3.web.descriptor.RequestMethodDescriptor;
import cn.procsl.ping.processor.v3.web.descriptor.RequestParameterDescriptor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

class ParameterDescriptorImpl implements RequestParameterDescriptor {

    final MethodDescriptorImpl requestMethodDescriptor;

    ParameterDescriptorImpl(MethodDescriptorImpl requestMethodDescriptor) {
        this.requestMethodDescriptor = requestMethodDescriptor;
    }

    @Override
    public RequestMethodDescriptor getMethodDescriptor() {
        return requestMethodDescriptor;
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        ExecutableElement target = this.requestMethodDescriptor.getTargetMethodElement();
        return target.getParameters();
    }
}
