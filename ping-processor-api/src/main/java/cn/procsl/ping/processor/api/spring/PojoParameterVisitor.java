package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.AbstractAnnotationVisitor;
import cn.procsl.ping.processor.api.AnnotationVisitor;
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
    public SupportType support() {
        return SupportType.CONTROLLER_PARAMETER;
    }

    @Override
    public void typeVisitor(Element element, TypeSpec.Builder target) {
        target.addAnnotation(getterBuilder);
        target.addAnnotation(setterBuilder);
        target.addAnnotation(validateAnnotation);
    }

    @Override
    public void fieldVisitor(Element element, FieldSpec.Builder spec) {
        Set<AnnotationSpec> annotations = element.getAnnotationMirrors()
            .stream()
            .filter(item -> item.getAnnotationType().toString().startsWith("javax.validation"))
            .map(AnnotationSpec::get)
            .collect(Collectors.toSet());
        spec.addAnnotations(annotations);
    }


}
