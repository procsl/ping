package cn.procsl.ping.processor.generator;

import javax.lang.model.element.VariableElement;

public interface ParameterAnnotationSpecBuilder extends DynamicAnnotationSpecBuilder<VariableElement> {

    @Override
    default Class<VariableElement> getType() {
        return VariableElement.class;
    }
}
