package cn.procsl.ping.processor.api;

import com.squareup.javapoet.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public interface GeneratorBuilder {

    void init(ProcessorContext context);

    boolean support(String type);

    void typeAnnotation(String type, Element element, TypeSpec.Builder spec);

    void fieldAnnotation(String type, Element element, FieldSpec.Builder spec);

    void methodAnnotation(String type, Element element, MethodSpec.Builder spec);

    void parameterAnnotation(String type, Element element, ParameterSpec.Builder spec);

    void variableAnnotation(String type, Element element, ParameterSpec.Builder spec);

    TypeName returnType(String type, ExecutableElement element);

    CodeBlock returnCodeBlack(String type, ExecutableElement element, CodeBlock caller);
}
