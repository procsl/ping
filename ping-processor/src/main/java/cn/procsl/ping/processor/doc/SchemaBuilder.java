package cn.procsl.ping.processor.doc;

import cn.procsl.ping.processor.web.AbstractAnnotationVisitor;
import cn.procsl.ping.processor.web.AnnotationVisitor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.lang.model.element.Element;

@AutoService(AnnotationVisitor.class)
public class SchemaBuilder extends AbstractAnnotationVisitor {

    final AnnotationSpec annotation = AnnotationSpec.builder(ClassName.get("io.swagger.v3.oas.annotations.media", "Schema")).build();

    @Override
    public void visitor(Element element, TypeSpec.Builder spec) {
        spec.addAnnotation(annotation);
    }

    @Override
    public void visitor(Element element, FieldSpec.Builder spec) {
        spec.addAnnotation(annotation);
    }

    @Override
    public void visitor(Element element, MethodSpec.Builder spec) {
    }

    @Override
    public void visitor(Element element, ParameterSpec.Builder spec) {
    }

    @Override
    public void variableVisitor(Element element, ParameterSpec.Builder spec) {
    }

    @Override
    public String support() {
        return "CONTROLLER_RETURNED";
    }

}
