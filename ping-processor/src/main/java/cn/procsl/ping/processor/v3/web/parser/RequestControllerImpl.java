package cn.procsl.ping.processor.v3.web.parser;


import cn.procsl.ping.processor.v3.FieldDescriptor;
import cn.procsl.ping.processor.v3.MethodDescriptor;
import cn.procsl.ping.processor.v3.TypeDescriptor;

import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;

class RequestControllerImpl implements TypeDescriptor {

    final TypeElement target;

    final List<FieldDescriptor> fieldDescriptors;

    final List<MethodDescriptor> methodDescriptors;

    RequestControllerImpl(TypeElement target,
                          List<FieldDescriptor> fieldDescriptors,
                          List<MethodDescriptor> methodDescriptors) {
        this.target = target;
        this.fieldDescriptors = fieldDescriptors;
        this.methodDescriptors = methodDescriptors;
    }


    @Override
    public String getPackageName() {
        return target.getQualifiedName().toString();
    }

    @Override
    public String getClassName() {
        return target.getSimpleName().toString();
    }

    @Override
    public TypeElement getTarget() {
        return this.target;
    }

    @Override
    public List<FieldDescriptor> getFields() {
        return Collections.unmodifiableList(this.fieldDescriptors);
    }

    @Override
    public List<MethodDescriptor> getMethods() {
        return Collections.unmodifiableList(this.methodDescriptors);
    }
}
