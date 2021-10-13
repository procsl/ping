package cn.procsl.ping.processor.restful.spring;

import cn.procsl.ping.processor.AbstractAnnotationVisitor;
import cn.procsl.ping.processor.AnnotationVisitor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(AnnotationVisitor.class)
public class PojoParameterVisitor extends AbstractAnnotationVisitor {


    final AnnotationSpec validateAnnotation = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.validation.annotation.Validated")).build();
    final AnnotationSpec getterBuilder = AnnotationSpec.builder(ClassName.bestGuess("lombok.Getter")).build();
    final AnnotationSpec setterBuilder = AnnotationSpec.builder(ClassName.bestGuess("lombok.Setter")).build();

    @Override
    public String support() {
        return "CONTROLLER_PARAMETER";
    }

    @Override
    public void visitor(Element element, TypeSpec.Builder target) {
        target.addAnnotation(getterBuilder);
        target.addAnnotation(setterBuilder);
        target.addAnnotation(validateAnnotation);
    }

    @Override
    public void visitor(Element element, FieldSpec.Builder spec) {
        Set<AnnotationSpec> annotations = element.getAnnotationMirrors()
            .stream()
            .filter(item -> item.getAnnotationType().toString().startsWith("javax.validation"))
            .map(AnnotationSpec::get)
            .collect(Collectors.toSet());
        spec.addAnnotations(annotations);
    }


}
