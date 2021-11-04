package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.component.*;
import cn.procsl.ping.processor.component.TypeNameComponent;
import com.squareup.javapoet.AnnotationSpec;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.lang.model.element.TypeElement;

public class ControllerRequestMappingAnnotationComponent implements AnnotationComponent<TypeElement> {

    @Override
    public AnnotationSpec generateStruct(ProcessorContext context, TypeElement element) {
        return AnnotationSpec.builder(RequestMapping.class).build();
    }

}
