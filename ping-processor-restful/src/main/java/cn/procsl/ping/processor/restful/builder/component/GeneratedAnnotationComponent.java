package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.Component;
import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.component.AnnotationComponent;
import cn.procsl.ping.processor.component.ClassTypeNameComponent;
import cn.procsl.ping.processor.component.CodeBlockComponent;
import cn.procsl.ping.processor.component.TypeNameComponent;
import com.squareup.javapoet.AnnotationSpec;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Generated;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class GeneratedAnnotationComponent implements AnnotationComponent<TypeElement> {

    final String generatorName;


    @Override
    public boolean addChild(Component<?, TypeElement> component) {
        return false;
    }

    @Override
    public boolean removeChild(Component<?, TypeElement> component) {
        return false;
    }

    @Override
    public Collection<Component<?, TypeElement>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "annotation";
    }

    @Override
    public AnnotationSpec generateStruct(ProcessorContext context, TypeElement element) {
        return AnnotationSpec.builder(Generated.class).addMember("value", "$S", generatorName).build();
    }


}
