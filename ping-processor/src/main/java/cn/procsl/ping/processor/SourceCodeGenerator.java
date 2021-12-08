package cn.procsl.ping.processor;

import com.squareup.javapoet.JavaFile;

import javax.lang.model.element.Element;

public interface SourceCodeGenerator {

    JavaFile generateStruct(JavaFile javaFile, Element targetElement);

    boolean isFinal();

}
