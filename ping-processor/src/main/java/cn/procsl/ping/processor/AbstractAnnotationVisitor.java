package cn.procsl.ping.processor;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

public abstract class AbstractAnnotationVisitor implements AnnotationVisitor {

    protected ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void typeVisitor(Element element, TypeSpec.Builder spec) {

    }

    @Override
    public void fieldVisitor(Element element, FieldSpec.Builder spec) {

    }

    @Override
    public void methodVisitor(Element element, MethodSpec.Builder spec) {

    }

    @Override
    public void parameterVisitor(Element element, ParameterSpec.Builder spec) {

    }

    @Override
    public void variableVisitor(Element element, ParameterSpec.Builder spec) {

    }
}

