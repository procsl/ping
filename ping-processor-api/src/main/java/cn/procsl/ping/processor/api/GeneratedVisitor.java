package cn.procsl.ping.processor.api;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

public interface GeneratedVisitor {

    void init(ProcessorContext context);

    String support();

    void typeVisitor(Element element, TypeSpec.Builder spec);

    void fieldVisitor(Element element, FieldSpec.Builder spec);

    void methodVisitor(Element element, MethodSpec.Builder spec);

    void parameterVisitor(Element element, ParameterSpec.Builder spec);

    void variableVisitor(Element element, ParameterSpec.Builder spec);

//    TypeName returnTypeVisitor(ExecutableElement element);
//
//    CodeBlock methodCodeBlackVisitor(ExecutableElement element, CodeBlock caller);
}
