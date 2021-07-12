package cn.procsl.ping.processor.api;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

public interface GeneratorBuilder {

    void init(ProcessorContext context);

    boolean support(String type);

    void typeAnnotation(String type, Element element, TypeSpec.Builder spec);

    void fieldAnnotation(String type, Element element, FieldSpec.Builder spec);

    void methodAnnotation(String type, Element element, MethodSpec.Builder spec);

    void parameterAnnotation(String type, Element element, ParameterSpec.Builder spec);

    void variableAnnotation(String type, Element element, ParameterSpec.Builder spec);

    void returnType(String type, Element element, MethodSpec.Builder spec);
}
