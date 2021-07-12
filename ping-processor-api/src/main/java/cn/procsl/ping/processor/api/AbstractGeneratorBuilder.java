package cn.procsl.ping.processor.api;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

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
    public void returnType(String type, Element element, MethodSpec.Builder spec) {

    }
}
