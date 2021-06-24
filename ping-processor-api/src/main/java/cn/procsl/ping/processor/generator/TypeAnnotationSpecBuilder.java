package cn.procsl.ping.processor.generator;

import javax.lang.model.element.TypeElement;

public interface TypeAnnotationSpecBuilder extends DynamicAnnotationSpecBuilder<TypeElement> {

    @Override
    default Class<TypeElement> getType() {
        return TypeElement.class;
    }
}
