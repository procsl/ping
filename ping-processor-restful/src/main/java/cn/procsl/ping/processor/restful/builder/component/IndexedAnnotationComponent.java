package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.component.*;
import cn.procsl.ping.processor.component.TypeNameComponent;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;
import org.springframework.stereotype.Indexed;

import javax.lang.model.element.TypeElement;

public class IndexedAnnotationComponent implements AnnotationComponent<TypeElement> {

    @Override
    public AnnotationSpec generateStruct(ProcessorContext context, TypeElement element) {
        AnnotationSpec builder = AnnotationSpec.builder(Indexed.class).build();
        return builder;
    }

}
