package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.component.AnnotationComponent;
import cn.procsl.ping.processor.component.ClassTypeNameComponent;
import cn.procsl.ping.processor.component.CodeBlockComponent;
import cn.procsl.ping.processor.component.TypeNameComponent;
import com.squareup.javapoet.AnnotationSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.TypeElement;

public class RestControllerAnnotationComponent implements AnnotationComponent<TypeElement> {

    @Override
    public AnnotationSpec generateStruct(ProcessorContext context, TypeElement element) {
        return AnnotationSpec.builder(RestController.class).build();
    }
}
