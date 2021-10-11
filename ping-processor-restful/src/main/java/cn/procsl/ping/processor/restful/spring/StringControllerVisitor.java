package cn.procsl.ping.processor.restful.spring;

import cn.procsl.ping.processor.AbstractAnnotationVisitor;
import cn.procsl.ping.processor.AnnotationVisitor;
import cn.procsl.ping.processor.restful.annotation.HttpStatus;
import cn.procsl.ping.processor.restful.model.ParameterVariableElement;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.ws.rs.GET;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(AnnotationVisitor.class)
public class StringControllerVisitor extends AbstractAnnotationVisitor {


    final static DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);
    final AnnotationSpec validate = AnnotationSpec.builder(ClassName.get("org.springframework.validation.annotation", "Validated")).build();
    final AnnotationSpec restController = AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "RestController")).build();
    final AnnotationSpec indexed = AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Indexed")).build();
    final AnnotationSpec transaction = AnnotationSpec
        .builder(ClassName.get("org.springframework.transaction.annotation", "Transactional"))
        .addMember("rollbackFor", "$T.class", Exception.class).build();
    final AnnotationSpec readOnlyTransaction = transaction.toBuilder()
        .addMember("readOnly", "$N", "true").build();
    final ClassName autowired = ClassName.get("org.springframework.beans.factory.annotation", "Autowired");
    final ClassName responseStatusName = ClassName.get("org.springframework.web.bind.annotation", "ResponseStatus");

    final RequestMappingAnnotationBuilder request = new RequestMappingAnnotationBuilder();

    @Override
    public String support() {
        return "CONTROLLER";
    }

    @Override
    public void typeVisitor(Element element, TypeSpec.Builder spec) {
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
    public void fieldVisitor(Element element, FieldSpec.Builder spec) {
        AnnotationSpec annotationSpec = AnnotationSpec.builder(autowired).addMember("required", "true").build();
        spec.addAnnotation(annotationSpec);
    }

    @Override
    public void methodVisitor(Element element, MethodSpec.Builder spec) {
        AnnotationSpec annotation = this.request.builder(element);
        spec.addAnnotation(annotation);

        if (!(element instanceof ExecutableElement)) {
            return;
        }

        if (element.getAnnotation(GET.class) != null) {
            spec.addAnnotation(readOnlyTransaction);
        } else {
            spec.addAnnotation(transaction);
        }

        AnnotationSpec.Builder statusAnnotation = AnnotationSpec.builder(responseStatusName);
        HttpStatus httpStatus = element.getAnnotation(HttpStatus.class);
        if (httpStatus != null) {
            statusAnnotation.addMember("code", "$S", String.valueOf(httpStatus.code()));
        }

        if (((ExecutableElement) element).getReturnType().getKind().toString().equals("VOID")) {
            statusAnnotation.addMember("code", "$N", "org.springframework.http.HttpStatus.NO_CONTENT");
            statusAnnotation.addMember("reason", "$S", "no content");
            spec.addAnnotation(statusAnnotation.build());
        }
    }

    @Override
    public void parameterVisitor(Element element, ParameterSpec.Builder spec) {
        if (element instanceof ParameterVariableElement) {
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
