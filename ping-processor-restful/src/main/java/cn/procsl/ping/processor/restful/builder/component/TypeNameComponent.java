package cn.procsl.ping.processor.restful.builder.component;

import lombok.RequiredArgsConstructor;

import javax.lang.model.element.TypeElement;

@RequiredArgsConstructor
public class TypeNameComponent implements cn.procsl.ping.processor.component.TypeNameComponent {

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
