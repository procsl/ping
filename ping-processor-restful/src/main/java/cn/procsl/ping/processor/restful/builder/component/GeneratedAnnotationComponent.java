package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.component.AnnotationComponent;
import cn.procsl.ping.processor.component.ClassTypeNameComponent;
import cn.procsl.ping.processor.component.CodeBlockComponent;
import cn.procsl.ping.processor.component.TypeNameComponent;

import javax.annotation.processing.Generated;

public class GeneratedAnnotationComponent implements AnnotationComponent {

    @Override
    public TypeNameComponent getType() {
        return new ClassTypeNameComponent(Generated.class);
    }

    @Override
    public CodeBlockComponent getCode() {
        return CodeBlockComponent.EMPTY_CODE_BLOCK_COMPONENT;
    }


}
