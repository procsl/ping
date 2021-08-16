package cn.procsl.ping.processor.api;

import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

public interface SourceCodeBuilder {

    void init(ProcessingEnvironment processingEnv);

    default TypeSpec toBuilder(String aPackage, TypeElement annotation, TypeSpec typeSpec, RoundEnvironment roundEnv) {
        return null;
    }

    boolean isSupport(String type);

    default int order() {
        return 0;
    }
}
