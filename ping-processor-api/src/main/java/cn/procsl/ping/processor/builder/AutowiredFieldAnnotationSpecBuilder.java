package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.Element;

@AutoService(value = AnnotationSpecBuilder.class)
public class AutowiredFieldAnnotationSpecBuilder extends AbstractAnnotationSpecBuilder<FieldSpec.Builder> {


    @Override
    protected boolean isType(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, FieldSpec.Builder target) {
        ClassName clazz = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired");
        AnnotationSpec annotationSpec = AnnotationSpec.builder(clazz).addMember("required", "true").build();
        target.addAnnotation(annotationSpec);
    }

    @Override
    protected Class<FieldSpec.Builder> target() {
        return FieldSpec.Builder.class;
    }

}
