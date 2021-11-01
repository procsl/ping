package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.component.AnnotationComponent;
import cn.procsl.ping.processor.component.ClassTypeNameComponent;
import cn.procsl.ping.processor.component.CodeBlockComponent;
import cn.procsl.ping.processor.component.TypeNameComponent;
import org.springframework.web.bind.annotation.RequestMapping;

public class RequestMappingAnnotationComponent implements AnnotationComponent {


    @Override
    public TypeNameComponent getType() {
        return new ClassTypeNameComponent(RequestMapping.class);
    }

    @Override
    public CodeBlockComponent getCode() {
        return CodeBlockComponent.EMPTY_CODE_BLOCK_COMPONENT;
    }
}
