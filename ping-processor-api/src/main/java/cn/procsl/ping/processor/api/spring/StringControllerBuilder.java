package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.AbstractGeneratorBuilder;
import cn.procsl.ping.processor.api.GeneratorBuilder;
import cn.procsl.ping.processor.api.syntax.VariableDTOElement;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(GeneratorBuilder.class)
public class StringControllerBuilder extends AbstractGeneratorBuilder {


    final AnnotationSpec validate = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.validation.annotation.Validated")).build();

    final AnnotationSpec restController = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")).build();

    final AnnotationSpec indexed = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.stereotype.Indexed")).build();

    final ClassName autowired = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired");

    final static DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);

    final RequestMappingAnnotation request = new RequestMappingAnnotation();

    @Override
    public boolean support(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    public void typeAnnotation(String type, Element element, TypeSpec.Builder spec) {
        String prefix = this.context.getConfig("processor.api.prefix");
        spec.addAnnotation(request.builder(prefix, element));
        spec.addAnnotation(validate);
        spec.addAnnotation(restController);
        spec.addAnnotation(indexed);

        AnnotationSpec generator = AnnotationSpec
            .builder(Generated.class)
            .addMember("value", "{$S}", format.format(new Date()))
            .build();
        spec.addAnnotation(generator);

    }

    @Override
    public void fieldAnnotation(String type, Element element, FieldSpec.Builder spec) {
        AnnotationSpec annotationSpec = AnnotationSpec.builder(autowired).addMember("required", "true").build();
        spec.addAnnotation(annotationSpec);
    }

    @Override
    public void methodAnnotation(String type, Element element, MethodSpec.Builder spec) {
        AnnotationSpec annotation = this.request.builder(element);
        spec.addAnnotation(annotation);
    }

    @Override
    public void parameterAnnotation(String type, Element element, ParameterSpec.Builder spec) {
        if (element instanceof VariableDTOElement) {
            spec.addAnnotation(this.validate);
            return;
        }

        Set<AnnotationSpec> annotations = element.getAnnotationMirrors()
            .stream()
            .filter(item -> item.getAnnotationType().toString().startsWith("javax.validation"))
            .map(AnnotationSpec::get)
            .collect(Collectors.toSet());
        spec.addAnnotations(annotations);
    }


}
