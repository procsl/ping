package cn.procsl.ping.processor.api;

import com.squareup.javapoet.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public abstract class AbstractGeneratorBuilder implements GeneratorBuilder {

    protected ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }


    @Override
    public void typeAnnotation(String type, Element element, TypeSpec.Builder spec) {

    }

    @Override
    public void fieldAnnotation(String type, Element element, FieldSpec.Builder spec) {

    }

    @Override
    public void methodAnnotation(String type, Element element, MethodSpec.Builder spec) {

    }

    @Override
    public void parameterAnnotation(String type, Element element, ParameterSpec.Builder spec) {

    }

    @Override
    public void variableAnnotation(String type, Element element, ParameterSpec.Builder spec) {

    }

    @Override
    public TypeName returnType(String type, ExecutableElement element) {
        return null;
    }

    @Override
    public CodeBlock returnCodeBlack(String type, ExecutableElement element, CodeBlock caller) {
        return null;
    }
}
