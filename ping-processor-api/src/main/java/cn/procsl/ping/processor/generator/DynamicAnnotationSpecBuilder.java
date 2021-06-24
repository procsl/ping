package cn.procsl.ping.processor.generator;

import com.squareup.javapoet.AnnotationSpec;

import javax.lang.model.element.Element;

public interface DynamicAnnotationSpecBuilder<S extends Element> extends SpecBuilder<S, AnnotationSpec> {

    Class<S> getType();

}
