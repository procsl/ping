package cn.procsl.ping.processor;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

public interface AnnotationVisitor {

    String support();

    void init(ProcessorContext context);

    void visitor(Element element, TypeSpec.Builder spec);

    void visitor(Element element, FieldSpec.Builder spec);

    void visitor(Element element, MethodSpec.Builder spec);

    void visitor(Element element, ParameterSpec.Builder spec);

    void variableVisitor(Element element, ParameterSpec.Builder spec);
}
