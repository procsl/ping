package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;

@AutoService(value = AnnotationSpecBuilder.class)
public class AutowiredFieldAnnotationSpecBuilder implements AnnotationSpecBuilder {


    @Override
    public <T extends Element> void build(ProcessorContext context, @Nullable T source, Object target, String type) {
        ClassName clazz = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired");
        AnnotationSpec annotationSpec = AnnotationSpec.builder(clazz).addMember("required", "true").build();
        if (target instanceof FieldSpec.Builder) {
            ((FieldSpec.Builder) target).addAnnotation(annotationSpec);
        }
    }
}
