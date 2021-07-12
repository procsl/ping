package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.AbstractGeneratorBuilder;
import cn.procsl.ping.processor.api.GeneratorBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(GeneratorBuilder.class)
public class DTOBuilder extends AbstractGeneratorBuilder {


    final AnnotationSpec validateAnnotation = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.validation.annotation.Validated")).build();
    final AnnotationSpec getterBuilder = AnnotationSpec.builder(ClassName.bestGuess("lombok.Getter")).build();
    final AnnotationSpec setterBuilder = AnnotationSpec.builder(ClassName.bestGuess("lombok.Setter")).build();

    @Override
    public void typeAnnotation(String type, Element element, TypeSpec.Builder target) {
        target.addAnnotation(getterBuilder);
        target.addAnnotation(setterBuilder);
        target.addAnnotation(validateAnnotation);
    }

    @Override
    public void fieldAnnotation(String type, Element element, FieldSpec.Builder spec) {
        Set<AnnotationSpec> annotations = element.getAnnotationMirrors()
            .stream()
            .filter(item -> item.getAnnotationType().toString().startsWith("javax.validation"))
            .map(AnnotationSpec::get)
            .collect(Collectors.toSet());
        spec.addAnnotations(annotations);
    }


    @Override
    public boolean support(String type) {
        return "DTO".equals(type);
    }


}
