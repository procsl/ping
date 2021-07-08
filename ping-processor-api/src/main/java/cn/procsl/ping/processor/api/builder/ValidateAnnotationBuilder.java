package cn.procsl.ping.processor.api.builder;

import cn.procsl.ping.processor.api.AbstractAnnotationSpecBuilder;
import cn.procsl.ping.processor.api.AnnotationSpecBuilder;
import cn.procsl.ping.processor.api.ProcessorContext;
import cn.procsl.ping.processor.api.syntax.VariableDTOElement;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(AnnotationSpecBuilder.class)
public class ValidateAnnotationBuilder extends AbstractAnnotationSpecBuilder<ParameterSpec.Builder> {

    final String validate = "org.springframework.validation.annotation.Validated";

    @Override
    protected boolean isType(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, ParameterSpec.Builder target) {
        if (source == null) {
            context.getMessager().printMessage(Diagnostic.Kind.WARNING, "找不到 VariableElement");
            return;
        }

        if (source instanceof VariableDTOElement) {
            AnnotationSpec validateAnnotation = AnnotationSpec.builder(ClassName.bestGuess(validate)).build();
            target.addAnnotation(validateAnnotation);
            return;
        }

        Set<AnnotationSpec> annotations = source.getAnnotationMirrors()
            .stream()
            .filter(item -> item.getAnnotationType().toString().startsWith("javax.validation"))
            .map(AnnotationSpec::get)
            .collect(Collectors.toSet());
        target.addAnnotations(annotations);
    }

    @Override
    protected Class<ParameterSpec.Builder> target() {
        return ParameterSpec.Builder.class;
    }

}
