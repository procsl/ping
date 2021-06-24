package cn.procsl.ping.processor.generator;

import javax.lang.model.element.ExecutableElement;

public interface MethodAnnotationSpecBuilder extends DynamicAnnotationSpecBuilder<ExecutableElement> {

    @Override
    default Class<ExecutableElement> getType() {
        return ExecutableElement.class;
    }
}
