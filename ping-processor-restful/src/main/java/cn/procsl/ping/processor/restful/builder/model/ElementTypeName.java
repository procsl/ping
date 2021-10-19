package cn.procsl.ping.processor.restful.builder.model;

import cn.procsl.ping.processor.model.TypeName;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.TypeElement;

@RequiredArgsConstructor
public class ElementTypeName implements TypeName {

    final TypeElement typeElement;

    @Override
    public String generatorType() {
        return "spring-type-element-name";
    }

    @Override
    public String getPackageName() {
        return typeElement.getEnclosingElement().toString();
    }

    @Override
    public String getClassName() {
        return typeElement.getSimpleName().toString();
    }
}
