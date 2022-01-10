package cn.procsl.ping.processor.v3.web.parser;

import cn.procsl.ping.processor.v3.MethodDescriptor;
import cn.procsl.ping.processor.v3.MethodParameterDescriptor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

class ParameterDescriptorImpl implements MethodParameterDescriptor {

    final MethodDescriptorImpl requestMethodDescriptor;

    ParameterDescriptorImpl(MethodDescriptorImpl requestMethodDescriptor) {
        this.requestMethodDescriptor = requestMethodDescriptor;
    }

    @Override
    public MethodDescriptor getMethodDescriptor() {
        return requestMethodDescriptor;
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        ExecutableElement target = this.requestMethodDescriptor.getTargetMethodElement();
        return target.getParameters();
    }
}
